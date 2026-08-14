package dev.wycobar.hegmark.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.collision.Ray;
import dev.wycobar.hegmark.feature.FeatureMutationResult;
import dev.wycobar.hegmark.feature.FeatureMutationStatus;
import dev.wycobar.hegmark.feature.elevation.ElevationFeature;
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
            if (uiButton != null && uiButton.enabled()) handle(uiButton);
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
                report(elevationFeature.fillGapsAt(cell, state.paintElevationMeters()));
            } else if (state.tool() == EditorTool.OVERWRITE) {
                FeatureMutationResult result = elevationFeature.overwriteAt(
                    cell,
                    state.paintElevationMeters(),
                    state.isOverwriteConfirmed(cell)
                );
                if (result.status() == FeatureMutationStatus.CONFIRMATION_REQUIRED) {
                    state.requestOverwriteConfirmation(cell, result.removedValues());
                } else {
                    state.clearOverwriteConfirmation();
                    report(result);
                }
            } else if (state.tool() == EditorTool.ERASE) {
                report(elevationFeature.eraseAt(cell));
            }
        } catch (IllegalArgumentException exception) {
            state.setMessage(exception.getMessage());
        }
        rebuildRequested = true;
    }

    private void report(FeatureMutationResult result) {
        switch (result.status()) {
            case APPLIED -> state.setMessage(
                "Elevation updated: " + result.changedValues() + " written, " + result.removedValues() + " removed"
            );
            case NO_CHANGE -> state.setMessage("Elevation unchanged");
            case REJECTED -> state.setMessage("Elevation cannot be edited at this resolution");
            case CONFIRMATION_REQUIRED -> throw new IllegalStateException("Confirmation result was not handled");
        }
    }

    private void handle(UiButton button) {
        switch (button.action()) {
            case SELECT_RENDERER -> {
                if (state.selectRenderer(button.targetId())) {
                    state.setMessage("Viewing " + button.label());
                    rebuildRequested = true;
                }
            }
            case SELECT_TOOL -> state.setTool(EditorTool.SELECT);
            case FILL_GAPS_TOOL -> state.setTool(EditorTool.FILL_GAPS);
            case OVERWRITE_TOOL -> state.setTool(EditorTool.OVERWRITE);
            case ERASE_TOOL -> state.setTool(EditorTool.ERASE);
            case ELEVATION_VALUE -> state.setPaintElevationMeters(Double.parseDouble(button.targetId()));
            case DETAIL_LESS -> orbitCamera.zoom(3.0f);
            case DETAIL_MORE -> orbitCamera.zoom(-3.0f);
            case ROTATE_LEFT -> orbitCamera.rotateLongitude(-1);
            case ROTATE_RIGHT -> orbitCamera.rotateLongitude(1);
            case ROTATE_UP -> orbitCamera.rotateLatitude(1);
            case ROTATE_DOWN -> orbitCamera.rotateLatitude(-1);
        }
    }
}
