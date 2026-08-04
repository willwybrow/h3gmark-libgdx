package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.H3PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetGrid;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.planet.PlanetModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElevationResolverTest {
    private PlanetGrid grid;
    private InMemoryFeatureValueStore store;
    private ElevationResolver resolver;
    private ElevationEditor editor;
    private PlanetModel planet;

    @BeforeEach
    void setUp() {
        grid = new H3PlanetGrid();
        store = new InMemoryFeatureValueStore();
        resolver = new ElevationResolver(grid, store, new ElevationGenerator());
        editor = new ElevationEditor(grid, store);
        planet = new PlanetModel("Test", 6_000_000.0, 5_900_000.0, 0.0, 99L);
    }

    @Test
    void generatedValuesAreDeterministic() {
        CellId cell = grid.cellAt(new PlanetLatLon(20.0, 30.0), 5);
        ResolvedElevation first = resolver.resolve(cell, planet);
        ElevationResolver independent = new ElevationResolver(grid, new InMemoryFeatureValueStore(), new ElevationGenerator());
        assertEquals(first, independent.resolve(cell, planet));
        PlanetModel otherSeed = new PlanetModel("Test", 6_000_000.0, 5_900_000.0, 0.0, 100L);
        assertNotEquals(first.meters(), independent.resolve(cell, otherSeed).meters());
        assertEquals(ElevationSource.GENERATED, resolver.resolve(cell, planet).source());
    }

    @Test
    void nearestExplicitValueOverridesCoarserAncestors() {
        CellId cell = grid.cellAt(new PlanetLatLon(20.0, 30.0), 6);
        CellId coarse = grid.parent(cell, 3);
        CellId nearer = grid.parent(cell, 5);
        editor.paint(coarse, 300.0);
        editor.paint(nearer, 900.0);

        ResolvedElevation resolved = resolver.resolve(cell, planet);
        assertEquals(900.0, resolved.meters());
        assertEquals(nearer, resolved.sourceCell().orElseThrow());
        assertEquals(ElevationSource.EXPLICIT, resolved.source());
    }

    @Test
    void directValueOverridesAncestorAndEraseRestoresInheritance() {
        CellId cell = grid.cellAt(new PlanetLatLon(20.0, 30.0), 6);
        CellId parent = grid.parent(cell, 5);
        editor.paint(parent, 300.0);
        editor.paint(cell, 1_200.0);
        assertEquals(1_200.0, resolver.resolve(cell, planet).meters());

        editor.erase(cell);
        assertEquals(300.0, resolver.resolve(cell, planet).meters());
        assertEquals(1, store.size());
    }

    @Test
    void finerCellsInheritMaximumApplicableAncestorAndCannotBeEdited() {
        CellId fine = grid.cellAt(new PlanetLatLon(20.0, 30.0), 8);
        CellId applicableParent = grid.parent(fine, 7);
        editor.paint(applicableParent, 750.0);

        ResolvedElevation resolved = resolver.resolve(fine, planet);
        assertEquals(750.0, resolved.meters());
        assertFalse(resolved.directlyEditable());
        assertEquals(applicableParent, resolved.sourceCell().orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> editor.paint(fine, 1_000.0));
        assertThrows(IllegalArgumentException.class, () -> editor.erase(fine));
    }

    @Test
    void coarserCellsAreNotApplicable() {
        CellId cell = grid.cellAt(new PlanetLatLon(20.0, 30.0), 1);
        ResolvedElevation resolved = resolver.resolve(cell, planet);
        assertFalse(resolved.applicable());
        assertEquals(ElevationSource.NOT_APPLICABLE, resolved.source());
    }
}
