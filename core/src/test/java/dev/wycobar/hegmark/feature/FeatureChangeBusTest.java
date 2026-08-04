package dev.wycobar.hegmark.feature;

import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeatureChangeBusTest {
    @Test
    void publishesSynchronouslyInRegistrationOrderUsingListenerSnapshot() {
        FeatureChangeBus bus = new FeatureChangeBus();
        List<String> calls = new ArrayList<>();
        FeatureChangeListener second = event -> calls.add("second");
        bus.subscribe(event -> {
            calls.add("first");
            bus.unsubscribe(second);
        });
        bus.subscribe(second);

        bus.publish(event());
        assertEquals(List.of("first", "second"), calls);

        calls.clear();
        bus.publish(event());
        assertEquals(List.of("first"), calls);
    }

    @Test
    void eventDefensivelyCopiesCellsAndRejectsEmptyChanges() {
        var cell = TestPlanetFactory.create().planet().cellAt(new PlanetLatLon(0.0, 0.0), 3);
        var changed = new java.util.HashSet<>(Set.of(cell));
        FeatureValuesChanged event = new FeatureValuesChanged(
            ElevationFeature.INSTANCE,
            FeatureMutationOperation.FILL_GAPS,
            changed,
            Set.of()
        );
        changed.clear();
        assertEquals(1, event.changedCells().size());
        assertThrows(UnsupportedOperationException.class, () -> event.changedCells().clear());
        assertThrows(
            IllegalArgumentException.class,
            () -> new FeatureValuesChanged(
                ElevationFeature.INSTANCE,
                FeatureMutationOperation.ERASE,
                Set.of(),
                Set.of()
            )
        );
    }

    @Test
    void listenerFailureDoesNotPreventCommittedChangeNotifications() {
        FeatureChangeBus bus = new FeatureChangeBus();
        List<String> calls = new ArrayList<>();
        bus.subscribe(event -> {
            throw new IllegalStateException("derived cache failed");
        });
        bus.subscribe(event -> calls.add("render"));

        var failures = bus.publish(event());
        assertEquals(1, failures.size());
        assertEquals(List.of("render"), calls);
    }

    private FeatureValuesChanged event() {
        var cell = TestPlanetFactory.create().planet().cellAt(new PlanetLatLon(0.0, 0.0), 3);
        return new FeatureValuesChanged(
            ElevationFeature.INSTANCE,
            FeatureMutationOperation.FILL_GAPS,
            Set.of(cell),
            Set.of()
        );
    }
}
