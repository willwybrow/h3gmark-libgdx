package dev.wycobar.hegmark.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditorLayoutTest {
    @Test
    void alwaysReservesBothPanes() {
        EditorLayout layout = new EditorLayout();
        layout.resize(1_200, 800);
        assertEquals(420, layout.panelWidth());
        assertEquals(780, layout.liveWidth());

        layout.resize(500, 400);
        assertEquals(250, layout.panelWidth());
        assertEquals(250, layout.liveWidth());

        layout.resize(100, 100);
        assertEquals(99, layout.panelWidth());
        assertEquals(1, layout.liveWidth());
    }

    @Test
    void hitRectangleIncludesEdgesAndRejectsOutsidePoints() {
        UiRect bounds = new UiRect(10, 20, 30, 40);
        assertTrue(bounds.contains(10, 20));
        assertTrue(bounds.contains(40, 60));
        assertFalse(bounds.contains(9, 20));
        assertFalse(bounds.contains(10, 61));
    }
}
