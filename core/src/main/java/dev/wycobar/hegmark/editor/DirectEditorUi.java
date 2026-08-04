package dev.wycobar.hegmark.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import dev.wycobar.hegmark.feature.ElevationResolver;
import dev.wycobar.hegmark.feature.ResolvedElevation;
import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;

import java.util.ArrayList;
import java.util.List;

public final class DirectEditorUi implements Disposable {
    private final PlanetGrid grid;
    private final PlanetModel planet;
    private final ElevationResolver resolver;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Matrix4 projection = new Matrix4();

    public DirectEditorUi(PlanetGrid grid, PlanetModel planet, ElevationResolver resolver) {
        this.grid = grid;
        this.planet = planet;
        this.resolver = resolver;
        font.getData().setScale(1.0f);
    }

    public List<UiButton> buttons(EditorLayout layout, EditorState state) {
        float x = layout.panelX() + 18.0f;
        float width = layout.panelWidth() - 36.0f;
        float third = (width - 12.0f) / 3.0f;
        List<UiButton> buttons = new ArrayList<>();
        buttons.add(button(UiAction.SELECT_TOOL, "Select", x, 58, third, state.tool() == EditorTool.SELECT, true));
        buttons.add(button(UiAction.PAINT_TOOL, "Paint", x + third + 6, 58, third, state.tool() == EditorTool.PAINT, true));
        buttons.add(button(UiAction.ERASE_TOOL, "Erase", x + (third + 6) * 2, 58, third, state.tool() == EditorTool.ERASE, true));

        float half = (width - 6.0f) / 2.0f;
        buttons.add(button(UiAction.ELEVATION_DEEP, "-1500 m", x, 126, half, state.paintElevationMeters() == -1_500.0, true));
        buttons.add(button(UiAction.ELEVATION_SEA, "0 m", x + half + 6, 126, half, state.paintElevationMeters() == 0.0, true));
        buttons.add(button(UiAction.ELEVATION_LOW, "+500 m", x, 164, half, state.paintElevationMeters() == 500.0, true));
        buttons.add(button(UiAction.ELEVATION_HIGH, "+2000 m", x + half + 6, 164, half, state.paintElevationMeters() == 2_000.0, true));
        buttons.add(button(UiAction.DETAIL_LESS, "Less detail", x, 220, half, false, true));
        buttons.add(button(UiAction.DETAIL_MORE, "More detail", x + half + 6, 220, half, false, true));
        return List.copyOf(buttons);
    }

    public void render(EditorLayout layout, EditorState state, int displayResolution, int renderedCells) {
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
        float x = layout.panelX() + 18.0f;
        draw("HEGMARK / ELEVATION", x, layout.height() - 20);
        draw("Tools", x, layout.height() - 48);
        draw("Paint value", x, layout.height() - 116);
        draw("View: grid res " + displayResolution + "  |  " + renderedCells + " cells", x, layout.height() - 210);
        draw("Editable range: res 2-7", x, layout.height() - 266);
        for (UiButton button : buttons(layout, state)) {
            font.setColor(button.enabled() ? Color.WHITE : Color.GRAY);
            float renderY = layout.height() - button.bounds().y() - button.bounds().height();
            draw(button.label(), button.bounds().x() + 8.0f, renderY + 20.0f);
        }
        font.setColor(Color.WHITE);
        drawSelection(state, x, layout.height() - 300);
        font.setColor(0.75f, 0.78f, 0.82f, 1.0f);
        draw(state.message(), x, 22);
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
        CellId cell = state.selectedCell().orElseThrow();
        PlanetLatLon center = grid.center(cell);
        ResolvedElevation elevation = resolver.resolve(cell, planet);
        int line = 0;
        draw("SELECTED CELL", x, startY - line++ * 22);
        draw("ID: " + cell.asHexString(), x, startY - line++ * 22);
        draw("Resolution: " + grid.resolution(cell), x, startY - line++ * 22);
        draw(String.format("Lat/Lon: %.3f / %.3f", center.latitudeDegrees(), center.longitudeDegrees()), x, startY - line++ * 22);
        draw("Pentagon: " + grid.isPentagon(cell), x, startY - line++ * 22);
        draw("Neighbors: " + grid.neighbors(cell).size(), x, startY - line++ * 22);
        if (grid.resolution(cell) > 0) draw("Parent: " + grid.parent(cell, grid.resolution(cell) - 1).asHexString(), x, startY - line++ * 22);
        draw(elevation.applicable() ? String.format("Elevation: %.0f m", elevation.meters()) : "Elevation: not applicable", x, startY - line++ * 22);
        draw("Source: " + elevation.source().name().toLowerCase(), x, startY - line++ * 22);
        draw("Directly editable: " + elevation.directlyEditable(), x, startY - line * 22);
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
