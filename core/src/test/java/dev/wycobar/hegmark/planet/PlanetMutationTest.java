package dev.wycobar.hegmark.planet;

import dev.wycobar.hegmark.feature.FeatureMutationOperation;
import dev.wycobar.hegmark.feature.FeatureValuesChanged;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanetMutationTest {
    @Test
    void fillGapsPreservesDescendantsAndPublishesOneBatch() {
        var fixture = TestPlanetFactory.create();
        Cell child = fixture.planet().cellAt(new PlanetLatLon(10.0, 20.0), 5);
        Cell parent = child.parent(3);
        fixture.planet().fillGaps(fixture.elevation(), List.of(child), -1_000.0);
        List<FeatureValuesChanged> events = new ArrayList<>();
        fixture.changes().subscribe(events::add);

        assertEquals(1, fixture.planet().fillGaps(fixture.elevation(), List.of(parent, parent), 500.0));
        assertEquals(-1_000.0, child.explicitFeature(fixture.elevation()).orElseThrow());
        assertEquals(500.0, parent.explicitFeature(fixture.elevation()).orElseThrow());
        assertEquals(1, events.size());
        assertEquals(FeatureMutationOperation.FILL_GAPS, events.getFirst().operation());
    }

    @Test
    void overwriteRemovesOnlyStrictFeatureDescendants() {
        var fixture = TestPlanetFactory.create();
        Cell child = fixture.planet().cellAt(new PlanetLatLon(10.0, 20.0), 5);
        Cell parent = child.parent(3);
        Cell sibling = parent.neighbors().getFirst();
        fixture.planet().fillGaps(fixture.elevation(), List.of(child), -1_000.0);
        fixture.planet().fillGaps(fixture.elevation(), List.of(sibling), 900.0);

        assertEquals(1, fixture.planet().overwrite(fixture.elevation(), parent, 700.0));
        assertTrue(child.explicitFeature(fixture.elevation()).isEmpty());
        assertEquals(700.0, parent.explicitFeature(fixture.elevation()).orElseThrow());
        assertEquals(900.0, sibling.explicitFeature(fixture.elevation()).orElseThrow());
    }

    @Test
    void eraseOnlyRemovesSelectedCellAndNoOpsDoNotPublish() {
        var fixture = TestPlanetFactory.create();
        Cell parent = fixture.planet().cellAt(new PlanetLatLon(10.0, 20.0), 4);
        Cell child = parent.children().getFirst();
        fixture.planet().fillGaps(fixture.elevation(), List.of(parent), 100.0);
        fixture.planet().fillGaps(fixture.elevation(), List.of(child), 200.0);
        List<FeatureValuesChanged> events = new ArrayList<>();
        fixture.changes().subscribe(events::add);

        assertTrue(fixture.planet().erase(fixture.elevation(), parent));
        assertEquals(200.0, child.explicitFeature(fixture.elevation()).orElseThrow());
        assertFalse(fixture.planet().erase(fixture.elevation(), parent));
        assertEquals(1, events.size());
    }

    @Test
    void rejectsEditsOutsideSettableRange() {
        var fixture = TestPlanetFactory.create();
        Cell coarse = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 1);
        Cell fine = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 8);
        assertThrows(IllegalArgumentException.class, () -> fixture.planet().fillGaps(fixture.elevation(), List.of(coarse), 1.0));
        assertThrows(IllegalArgumentException.class, () -> fixture.planet().overwrite(fixture.elevation(), fine, 1.0));
    }

    @Test
    void retainsPostCommitNotificationFailuresForInspection() {
        var fixture = TestPlanetFactory.create();
        fixture.changes().subscribe(event -> {
            throw new IllegalStateException("renderer failed");
        });
        Cell cell = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 3);
        assertEquals(1, fixture.planet().fillGaps(fixture.elevation(), List.of(cell), 10.0));
        assertEquals(1, fixture.planet().lastNotificationFailures().size());
        assertEquals(10.0, cell.explicitFeature(fixture.elevation()).orElseThrow());
    }
}
