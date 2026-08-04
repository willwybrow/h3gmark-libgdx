package dev.wycobar.hegmark.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.collision.Ray;
import dev.wycobar.hegmark.feature.ElevationFeature;
import dev.wycobar.hegmark.planet.CartesianPoint;
import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;
import dev.wycobar.hegmark.planet.Planet;
import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.render.OrbitCamera;

import java.util.Optional;
import java.util.function.IntSupplier;

public final class EditorInputController extends InputAdapter {
    private final EditorLayout layout;
    private final EditorState state;
    private final DirectEditorUi ui;
    private final OrbitCamera orbitCamera;
    private final PlanetRayPicker picker;
    private final PlanetModel planet;
    private final Planet world;
    private final ElevationFeature elevationFeature;
    private final IntSupplier displayResolution;
    private boolean orbiting;
    private int lastX;
    private int lastY;
    private boolean rebuildRequested = true;

    public EditorInputController(
        EditorLayout layout,
        EditorState state,
        DirectEditorUi ui,
        OrbitCamera orbitCamera,
        PlanetRayPicker picker,
        PlanetModel planet,
        Planet world,
        ElevationFeature elevationFeature,
        IntSupplier displayResolution
    ) {
        this.layout = layout;
        this.state = state;
        this.ui = ui;
        this.orbitCamera = orbitCamera;
        this.picker = picker;
        this.planet = planet;
        this.world = world;
        this.elevationFeature = elevationFeature;
        this.displayResolution = displayResolution;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (screenX >= layout.liveWidth() && button == Input.Buttons.LEFT) {
            UiButton uiButton = ui.buttonAt(layout, state, screenX, screenY);
            if (uiButton != null && uiButton.enabled()) handle(uiButton.action());
            return true;
        }
        if (screenX < layout.liveWidth() && button == Input.Buttons.RIGHT) {
            orbiting = true;
            lastX = screenX;
            lastY = screenY;
            return true;
        }
        if (screenX < layout.liveWidth() && button == Input.Buttons.LEFT) {
            selectOrEdit(screenX, screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!orbiting) return false;
        orbitCamera.orbit(screenX - lastX, screenY - lastY);
        lastX = screenX;
        lastY = screenY;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) orbiting = false;
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (Gdx.input.getX() >= layout.liveWidth()) return false;
        orbitCamera.zoom(amountY);
        return true;
    }

    public boolean consumeRebuildRequest() {
        boolean requested = rebuildRequested;
        rebuildRequested = false;
        return requested;
    }

    public void requestRebuild() {
        rebuildRequested = true;
    }

    private void selectOrEdit(int screenX, int screenY) {
        Ray ray = orbitCamera.camera().getPickRay(
            screenX,
            screenY,
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
        if (coordinate.isEmpty()) {
            state.setMessage("No cell under cursor");
            return;
        }
        Cell cell = world.cellAt(coordinate.orElseThrow(), displayResolution.getAsInt());
        state.select(cell);
        try {
            if (state.tool() == EditorTool.FILL_GAPS) {
                if (world.fillGaps(elevationFeature, java.util.List.of(cell), state.paintElevationMeters()) == 1) {
                    state.setMessage("Filled gaps with " + Math.round(state.paintElevationMeters()) + " m");
                } else {
                    state.setMessage("Cell already has that stored value");
                }
            } else if (state.tool() == EditorTool.OVERWRITE) {
                int descendantCount = cell.explicitDescendantCount(elevationFeature);
                if (descendantCount == 0 || state.isOverwriteConfirmed(cell)) {
                    int removed = world.overwrite(elevationFeature, cell, state.paintElevationMeters());
                    state.clearOverwriteConfirmation();
                    state.setMessage("Overwrote region and removed " + removed + " descendant values");
                } else {
                    state.requestOverwriteConfirmation(cell, descendantCount);
                    rebuildRequested = true;
                    return;
                }
            } else if (state.tool() == EditorTool.ERASE) {
                if (world.erase(elevationFeature, cell)) state.setMessage("Erased direct elevation override");
                else state.setMessage("Cell has no direct elevation override");
            }
        } catch (IllegalArgumentException exception) {
            state.setMessage(exception.getMessage());
        }
        rebuildRequested = true;
    }

    private void handle(UiAction action) {
        switch (action) {
            case SELECT_TOOL -> state.setTool(EditorTool.SELECT);
            case FILL_GAPS_TOOL -> state.setTool(EditorTool.FILL_GAPS);
            case OVERWRITE_TOOL -> state.setTool(EditorTool.OVERWRITE);
            case ERASE_TOOL -> state.setTool(EditorTool.ERASE);
            case ELEVATION_DEEP -> state.setPaintElevationMeters(-1_500.0);
            case ELEVATION_SEA -> state.setPaintElevationMeters(0.0);
            case ELEVATION_LOW -> state.setPaintElevationMeters(500.0);
            case ELEVATION_HIGH -> state.setPaintElevationMeters(2_000.0);
            case DETAIL_LESS -> orbitCamera.zoom(3.0f);
            case DETAIL_MORE -> orbitCamera.zoom(-3.0f);
            case ROTATE_LEFT -> orbitCamera.rotateLongitude(displayResolution.getAsInt(), -1);
            case ROTATE_RIGHT -> orbitCamera.rotateLongitude(displayResolution.getAsInt(), 1);
        }
    }
}
