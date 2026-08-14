package dev.wycobar.hegmark.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import dev.wycobar.hegmark.feature.elevation.ElevationFeature;
import dev.wycobar.hegmark.planet.Layer;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.render.RenderableFeature;

import java.util.ArrayList;
import java.util.List;

public final class DirectEditorUi implements Disposable {
    private final ElevationFeature elevationFeature;
    private final List<RenderableFeature> renderableFeatures;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Matrix4 projection = new Matrix4();
    private Layer displayResolution = Layer.COUNTRY;

    public DirectEditorUi(
        ElevationFeature elevationFeature,
        List<RenderableFeature> renderableFeatures
    ) {
        this.elevationFeature = elevationFeature;
        this.renderableFeatures = List.copyOf(renderableFeatures);
        font.getData().setScale(1.0f);
    }

    public List<UiButton> buttons(EditorLayout layout, EditorState state) {
        float padding = Math.min(18.0f, layout.panelWidth() / 8.0f);
        float x = layout.panelX() + padding;
        float width = Math.max(1.0f, layout.panelWidth() - padding * 2.0f);
        float quarter = (width - 18.0f) / 4.0f;
        boolean editable = elevationFeature.isSettableAt(displayResolution);
        List<UiButton> buttons = new ArrayList<>();
        float half = (width - 6.0f) / 2.0f;
        for (int index = 0; index < renderableFeatures.size(); index++) {
            RenderableFeature feature = renderableFeatures.get(index);
            float rendererX = x + (index % 2) * (half + 6.0f);
            float rendererY = 38.0f + (index / 2) * 36.0f;
            buttons.add(new UiButton(
                UiAction.FEATURE_RENDERER,
                feature.id(),
                feature.name(),
                new UiRect(rendererX, rendererY, half, 30.0f),
                feature.id().equals(state.activeRendererId()),
                true
            ));
        }

        float rendererRows = Math.max(1, (renderableFeatures.size() + 1) / 2);
        float toolY = 44.0f + rendererRows * 36.0f;
        buttons.add(button(UiAction.SELECT_TOOL, "Select", x, toolY, quarter, state.tool() == EditorTool.SELECT, true));
        buttons.add(button(UiAction.FILL_GAPS_TOOL, "Fill", x + quarter + 6, toolY, quarter, state.tool() == EditorTool.FILL_GAPS, editable));
        buttons.add(button(UiAction.OVERWRITE_TOOL, "Overwrite", x + (quarter + 6) * 2, toolY, quarter, state.tool() == EditorTool.OVERWRITE, editable));
        buttons.add(button(UiAction.ERASE_TOOL, "Erase", x + (quarter + 6) * 3, toolY, quarter, state.tool() == EditorTool.ERASE, editable));

        float paintY = toolY + 48.0f;
        buttons.add(button(UiAction.ELEVATION_DEEP, "-1500 m", x, paintY, half, state.paintElevationMeters() == -1_500.0, true));
        buttons.add(button(UiAction.ELEVATION_SEA, "0 m", x + half + 6, paintY, half, state.paintElevationMeters() == 0.0, true));
        buttons.add(button(UiAction.ELEVATION_LOW, "+500 m", x, paintY + 36, half, state.paintElevationMeters() == 500.0, true));
        buttons.add(button(UiAction.ELEVATION_HIGH, "+2000 m", x + half + 6, paintY + 36, half, state.paintElevationMeters() == 2_000.0, true));
        buttons.add(button(UiAction.DETAIL_LESS, "Zoom out", x, paintY + 78, half, false, true));
        buttons.add(button(UiAction.DETAIL_MORE, "Zoom in", x + half + 6, paintY + 78, half, false, true));
        buttons.add(button(UiAction.ROTATE_LEFT, "Rotate left", x, paintY + 114, half, false, true));
        buttons.add(button(UiAction.ROTATE_RIGHT, "Rotate right", x + half + 6, paintY + 114, half, false, true));
        buttons.add(button(UiAction.ROTATE_UP, "Rotate up", x, paintY + 150, half, false, true));
        buttons.add(button(UiAction.ROTATE_DOWN, "Rotate down", x + half + 6, paintY + 150, half, false, true));
        return List.copyOf(buttons);
    }

    public void render(EditorLayout layout, EditorState state, Layer displayResolution, int renderedCells) {
        this.displayResolution = displayResolution;
        projection.setToOrtho2D(0, 0, layout.width(), layout.height());
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.11f, 0.12f, 0.14f, 1.0f);
        shapes.rect(layout.panelX(), 0, layout.panelWidth(), layout.height());
        shapes.setColor(0.26f, 0.28f, 0.31f, 1.0f);
        shapes.rect(layout.panelX(), 0, 2, layout.height());
        for (UiButton button : buttons(layout, state)) drawButton(layout, button);
        shapes.end();

        batch.setProjectionMatrix(projection);
        batch.begin();
        font.setColor(Color.WHITE);
        float x = layout.panelX() + Math.min(18.0f, layout.panelWidth() / 8.0f);
        draw("HEGMARK / FEATURES", x, layout.height() - 20);
        draw("View: grid res " + displayResolution + "  |  " + renderedCells + " cells", x, layout.height() - 286);
        draw(
            "View " + elevationFeature.viewableRange().minimum() + "-" + elevationFeature.viewableRange().maximum()
                + "  |  Set " + elevationFeature.settableRange().orElseThrow().minimum()
                + "-" + elevationFeature.settableRange().orElseThrow().maximum(),
            x,
            layout.height() - 302
        );
        for (UiButton button : buttons(layout, state)) {
            font.setColor(button.enabled() ? Color.WHITE : Color.GRAY);
            float renderY = layout.height() - button.bounds().y() - button.bounds().height();
            draw(button.label(), button.bounds().x() + 8.0f, renderY + 20.0f);
        }
        font.setColor(Color.WHITE);
        drawSelection(state, x, layout.height() - 310);
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.08f, 0.09f, 0.11f, 1.0f);
        shapes.rect(layout.panelX(), 0, layout.panelWidth(), 30.0f);
        shapes.end();
        batch.begin();
        font.setColor(0.75f, 0.78f, 0.82f, 1.0f);
        draw(state.message(), x, 20);
        batch.end();
    }

    public UiButton buttonAt(EditorLayout layout, EditorState state, float screenX, float screenY) {
        return buttons(layout, state).stream().filter(button -> button.bounds().contains(screenX, screenY)).findFirst().orElse(null);
    }

    private void drawSelection(EditorState state, float x, float startY) {
        if (state.selectedCell().isEmpty()) {
            draw("No selected cell", x, startY);
            return;
        }
        Cell cell = state.selectedCell().orElseThrow();
        PlanetLatLon center = cell.center();
        int line = 0;
        float spacing = 13.0f;
        draw("SELECTED CELL", x, startY - line++ * spacing);
        draw("ID: " + cell.id().asHexString(), x, startY - line++ * spacing);
        draw("Resolution: " + cell.resolution(), x, startY - line++ * spacing);
        draw(String.format("Lat/Lon: %.3f / %.3f", center.latitudeDegrees(), center.longitudeDegrees()), x, startY - line++ * spacing);
        draw("Neighbors: " + cell.neighbours().size(), x, startY - line++ * spacing);
        if (cell.parent().isPresent()) draw("Parent: " + cell.parent().orElseThrow().id().asHexString(), x, startY - line++ * spacing);
        draw(String.format("Elevation: %.0f m", elevationFeature.valueAt(cell)), x, startY - line * spacing);

    }

    private void drawButton(EditorLayout layout, UiButton button) {
        UiRect bounds = button.bounds();
        float renderY = layout.height() - bounds.y() - bounds.height();
        if (!button.enabled()) shapes.setColor(0.18f, 0.19f, 0.21f, 1.0f);
        else if (button.active()) shapes.setColor(0.24f, 0.53f, 0.34f, 1.0f);
        else shapes.setColor(0.27f, 0.29f, 0.33f, 1.0f);
        shapes.rect(bounds.x(), renderY, bounds.width(), bounds.height());
    }

    private UiButton button(UiAction action, String label, float x, float y, float width, boolean active, boolean enabled) {
        return new UiButton(action, label, new UiRect(x, y, width, 30.0f), active, enabled);
    }

    private void draw(String text, float x, float y) {
        font.draw(batch, text, x, y);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
