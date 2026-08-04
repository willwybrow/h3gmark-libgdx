package dev.wycobar.hegmark;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.collision.Ray;
import dev.wycobar.hegmark.editor.DirectEditorUi;
import dev.wycobar.hegmark.editor.EditorInputController;
import dev.wycobar.hegmark.editor.EditorLayout;
import dev.wycobar.hegmark.editor.EditorState;
import dev.wycobar.hegmark.editor.LodPolicy;
import dev.wycobar.hegmark.editor.PlanetRayPicker;
import dev.wycobar.hegmark.editor.SelectionProjector;
import dev.wycobar.hegmark.editor.VisibleCellSelector;
import dev.wycobar.hegmark.feature.ElevationEditor;
import dev.wycobar.hegmark.feature.ElevationGenerator;
import dev.wycobar.hegmark.feature.ElevationResolver;
import dev.wycobar.hegmark.feature.ElevationStyle;
import dev.wycobar.hegmark.feature.InMemoryFeatureValueStore;
import dev.wycobar.hegmark.planet.CartesianPoint;
import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.H3PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;
import dev.wycobar.hegmark.render.CellSurfaceRenderer;
import dev.wycobar.hegmark.render.OrbitCamera;

import java.util.List;
import java.util.Optional;

public class Main extends ApplicationAdapter {
    private final EditorLayout layout = new EditorLayout();
    private final EditorState state = new EditorState();
    private final LodPolicy lodPolicy = new LodPolicy();
    private final PlanetRayPicker picker = new PlanetRayPicker();

    private PlanetModel planet;
    private PlanetGrid grid;
    private OrbitCamera orbitCamera;
    private VisibleCellSelector visibleCells;
    private SelectionProjector selectionProjector;
    private CellSurfaceRenderer surfaceRenderer;
    private DirectEditorUi ui;
    private EditorInputController input;
    private int displayResolution = 2;
    private CellId focusCell;

    @Override
    public void create() {
        orbitCamera = new OrbitCamera();
        planet = new PlanetModel("Hegmark", 6_400_000.0, 6_200_000.0, 0.0, 0x48a9dL);
        grid = new H3PlanetGrid();
        InMemoryFeatureValueStore store = new InMemoryFeatureValueStore();
        ElevationResolver elevationResolver = new ElevationResolver(grid, store, new ElevationGenerator());
        ElevationEditor elevationEditor = new ElevationEditor(grid, store);
        visibleCells = new VisibleCellSelector(grid);
        selectionProjector = new SelectionProjector(grid);
        surfaceRenderer = new CellSurfaceRenderer(grid, planet, elevationResolver, new ElevationStyle());
        ui = new DirectEditorUi(grid, planet, elevationResolver);
        input = new EditorInputController(
            layout,
            state,
            ui,
            orbitCamera,
            picker,
            planet,
            grid,
            elevationEditor,
            () -> displayResolution
        );
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void render() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        layout.resize(width, height);
        orbitCamera.update(layout.liveWidth(), layout.height());

        PlanetLatLon viewCenter = viewCenter();
        int nextResolution = lodPolicy.resolution(distanceToSurface(viewCenter));
        CellId nextFocus = grid.cellAt(viewCenter, nextResolution);
        boolean focusChanged = !nextFocus.equals(focusCell);
        boolean rebuild = input.consumeRebuildRequest()
            || nextResolution != displayResolution
            || (nextResolution > 2 && focusChanged);
        displayResolution = nextResolution;
        focusCell = nextFocus;
        if (rebuild) {
            List<CellId> cells = visibleCells.select(focusCell, displayResolution);
            CellId displayedSelection = state.selectedCell()
                .map(cell -> selectionProjector.atResolution(cell, displayResolution))
                .orElse(null);
            surfaceRenderer.rebuild(cells, displayedSelection);
        }

        Gdx.gl.glViewport(0, 0, width, height);
        Gdx.gl.glClearColor(0.025f, 0.03f, 0.045f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glViewport(0, 0, layout.liveWidth(), layout.height());
        surfaceRenderer.render(orbitCamera.camera());

        Gdx.gl.glViewport(0, 0, width, height);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        ui.render(layout, state, displayResolution, surfaceRenderer.cellCount());
    }

    @Override
    public void resize(int width, int height) {
        layout.resize(width, height);
        orbitCamera.update(layout.liveWidth(), layout.height());
        if (input != null) input.requestRebuild();
    }

    private PlanetLatLon viewCenter() {
        Ray ray = orbitCamera.camera().getPickRay(
            layout.liveWidth() / 2.0f,
            layout.height() / 2.0f,
            0.0f,
            0.0f,
            layout.liveWidth(),
            layout.height()
        );
        Optional<PlanetLatLon> coordinate = picker.pick(
            new CartesianPoint(ray.origin.x, ray.origin.y, ray.origin.z),
            new CartesianPoint(ray.direction.x, ray.direction.y, ray.direction.z),
            planet
        );
        return coordinate.orElse(new PlanetLatLon(0.0, 0.0));
    }

    private double distanceToSurface(PlanetLatLon viewCenter) {
        CartesianPoint surface = planet.toRenderCartesian(viewCenter);
        double deltaX = orbitCamera.camera().position.x - surface.x();
        double deltaY = orbitCamera.camera().position.y - surface.y();
        double deltaZ = orbitCamera.camera().position.z - surface.z();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    @Override
    public void dispose() {
        surfaceRenderer.dispose();
        ui.dispose();
    }
}
