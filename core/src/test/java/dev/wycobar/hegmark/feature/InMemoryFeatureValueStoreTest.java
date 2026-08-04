package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryFeatureValueStoreTest {
    @Test
    void storesTypedSparseFeatureValuesAndReturnsImmutableSnapshots() {
        InMemoryFeatureValueStore store = new InMemoryFeatureValueStore();
        CellId cell = new CellId(7L);
        store.put(ElevationFeature.INSTANCE, cell, 100.0);

        assertEquals(100.0, store.value(ElevationFeature.INSTANCE, cell).orElseThrow());
        assertEquals(1, store.values(ElevationFeature.INSTANCE).size());
        assertEquals(new ExplicitFeatureValue<>(ElevationFeature.INSTANCE, 100.0), store.values(cell).getFirst());
        assertThrows(
            UnsupportedOperationException.class,
            () -> store.values(ElevationFeature.INSTANCE).clear()
        );

        store.remove(ElevationFeature.INSTANCE, cell);
        assertTrue(store.value(ElevationFeature.INSTANCE, cell).isEmpty());
        assertEquals(0, store.size());
    }

    @Test
    void delegatesValueValidationToFeatureOwner() {
        InMemoryFeatureValueStore store = new InMemoryFeatureValueStore();
        assertThrows(
            IllegalArgumentException.class,
            () -> store.put(ElevationFeature.INSTANCE, new CellId(1L), Double.NaN)
        );
    }
}
