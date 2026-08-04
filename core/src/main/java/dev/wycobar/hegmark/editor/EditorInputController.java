package dev.wycobar.hegmark.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.collision.Ray;
import dev.wycobar.hegmark.feature.ElevationEditor;
import dev.wycobar.hegmark.planet.CartesianPoint;
import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;
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
    private final PlanetGrid grid;
    private final ElevationEditor elevationEditor;
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
        PlanetGrid grid,
        ElevationEditor elevationEditor,
        IntSupplier displayResolution
    ) {
        this.layout = layout;
        this.state = state;
        this.ui = ui;
        this.orbitCamera = orbitCamera;
        this.picker = picker;
        this.planet = planet;
        this.grid = grid;
        this.elevationEditor = elevationEditor;
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
        rebuildRequested = true;
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
        CellId cell = grid.cellAt(coordinate.orElseThrow(), displayResolution.getAsInt());
        state.select(cell);
        try {
            if (state.tool() == EditorTool.PAINT) {
                elevationEditor.paint(cell, state.paintElevationMeters());
                state.setMessage("Painted " + Math.round(state.paintElevationMeters()) + " m");
            } else if (state.tool() == EditorTool.ERASE) {
                elevationEditor.erase(cell);
                state.setMessage("Erased direct elevation override");
            }
        } catch (IllegalArgumentException exception) {
            state.setMessage(exception.getMessage());
        }
        rebuildRequested = true;
    }

    private void handle(UiAction action) {
        switch (action) {
            case SELECT_TOOL -> state.setTool(EditorTool.SELECT);
            case PAINT_TOOL -> state.setTool(EditorTool.PAINT);
            case ERASE_TOOL -> state.setTool(EditorTool.ERASE);
            case ELEVATION_DEEP -> state.setPaintElevationMeters(-1_500.0);
            case ELEVATION_SEA -> state.setPaintElevationMeters(0.0);
            case ELEVATION_LOW -> state.setPaintElevationMeters(500.0);
            case ELEVATION_HIGH -> state.setPaintElevationMeters(2_000.0);
            case DETAIL_LESS -> orbitCamera.zoom(3.0f);
            case DETAIL_MORE -> orbitCamera.zoom(-3.0f);
        }
        rebuildRequested = true;
    }
}
