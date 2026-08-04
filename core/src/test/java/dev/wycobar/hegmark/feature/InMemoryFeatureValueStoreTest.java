package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryFeatureValueStoreTest {
    @Test
    void keepsFeatureAndCellKeysIndependentAndSparse() {
        InMemoryFeatureValueStore store = new InMemoryFeatureValueStore();
        CellId cell = new CellId(7L);
        store.put("elevation", cell, 100.0);
        store.put("temperature", cell, 20.0);

        assertEquals(100.0, store.value("elevation", cell).orElseThrow());
        assertEquals(20.0, store.value("temperature", cell).orElseThrow());
        assertEquals(2, store.size());

        store.remove("elevation", cell);
        assertTrue(store.value("elevation", cell).isEmpty());
        assertEquals(1, store.size());
    }

    @Test
    void rejectsNonFiniteValues() {
        InMemoryFeatureValueStore store = new InMemoryFeatureValueStore();
        assertThrows(IllegalArgumentException.class, () -> store.put("elevation", new CellId(1L), Double.NaN));
    }
}
