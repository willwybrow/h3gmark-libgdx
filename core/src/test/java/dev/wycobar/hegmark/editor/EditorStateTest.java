package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorStateTest {
    @Test
    void rendererSelectionReportsChanges() {
        EditorState state = new EditorState();

        assertTrue(state.selectRenderer("elevation-renderer"));
        assertFalse(state.selectRenderer("elevation-renderer"));
        assertTrue(state.selectRenderer("land-renderer"));
        assertEquals("land-renderer", state.activeRendererId());
    }

    @Test
    void overwriteConfirmationIsScopedToCellAndPaintValue() {
        EditorState state = new EditorState();
        var fixture = TestPlanetFactory.create();
        var cell = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 3);
        state.requestOverwriteConfirmation(cell, 3);
        assertTrue(state.isOverwriteConfirmed(cell));
        assertFalse(state.isOverwriteConfirmed(cell.neighbours().getFirst()));
        state.select(cell.neighbours().getFirst());
        assertFalse(state.isOverwriteConfirmed(cell));

        state.setPaintElevationMeters(2_000.0);
        assertFalse(state.isOverwriteConfirmed(cell));
        state.requestOverwriteConfirmation(cell, 3);
        state.setTool(EditorTool.SELECT);
        assertFalse(state.isOverwriteConfirmed(cell));
    }
}
