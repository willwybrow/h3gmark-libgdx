package dev.wycobar.hegmark.feature.elevation;

import dev.wycobar.hegmark.feature.FeatureMutationStatus;
import dev.wycobar.hegmark.feature.FeatureValuesChanged;
import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.planet.PlanetLatLon;
import dev.wycobar.hegmark.support.TestPlanetFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ElevationFeatureTest {
    @Test
    void unsetCellsHaveAValueAndChildrenInheritAnExplicitAncestor() {
        var fixture = TestPlanetFactory.create();
        Cell parent = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 2);
        Cell child = parent.children().getFirst();

        assertEquals(0.0, fixture.elevation().valueAt(parent));

        var result = fixture.elevation().fillGapsAt(parent, 1_500.0);

        assertEquals(FeatureMutationStatus.APPLIED, result.status());
        assertEquals(1_500.0, fixture.elevation().valueAt(parent));
        assertEquals(1_500.0, fixture.elevation().valueAt(child));
    }

    @Test
    void writingBelowAnExplicitAncestorSplitsOnlyThePathToTheTarget() {
        var fixture = TestPlanetFactory.create();
        Cell ancestor = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 2);
        Cell branch = ancestor.children().getFirst();
        Cell target = branch.children().getFirst();
        fixture.elevation().fillGapsAt(ancestor, 400.0);

        fixture.elevation().fillGapsAt(target, 1_500.0);

        assertNull(fixture.values().getDouble(fixture.elevation(), ancestor.id()));
        assertNull(fixture.values().getDouble(fixture.elevation(), branch.id()));
        assertEquals(1_500.0, fixture.values().getDouble(fixture.elevation(), target.id()));
        for (Cell sibling : ancestor.children()) {
            if (!sibling.equals(branch)) {
                assertEquals(400.0, fixture.values().getDouble(fixture.elevation(), sibling.id()));
            }
        }
        for (Cell sibling : branch.children()) {
            if (!sibling.equals(target)) {
                assertEquals(400.0, fixture.values().getDouble(fixture.elevation(), sibling.id()));
                assertEquals(400.0, fixture.elevation().valueAt(sibling));
            }
        }
        assertEquals(1_500.0, fixture.elevation().valueAt(target));
    }

    @Test
    void equalValuesOnEveryChildAggregateToTheSameParentValue() {
        var fixture = TestPlanetFactory.create();
        Cell parent = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 2);
        fixture.elevation().fillGapsAt(parent, 200.0);

        for (Cell child : parent.children()) {
            fixture.elevation().fillGapsAt(child, 1_500.0);
        }

        assertNull(fixture.values().getDouble(fixture.elevation(), parent.id()));
        assertEquals(1_500.0, fixture.elevation().valueAt(parent), 0.000_001);
    }

    @Test
    void fillGapsPreservesExistingDetailAndWritesOnlyUncoveredBranches() {
        var fixture = TestPlanetFactory.create();
        Cell parent = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 2);
        Cell detailedChild = parent.children().getFirst();
        fixture.elevation().fillGapsAt(detailedChild, 900.0);

        fixture.elevation().fillGapsAt(parent, 100.0);

        assertNull(fixture.values().getDouble(fixture.elevation(), parent.id()));
        assertEquals(900.0, fixture.values().getDouble(fixture.elevation(), detailedChild.id()));
        for (Cell child : parent.children()) {
            if (!child.equals(detailedChild)) {
                assertEquals(100.0, fixture.values().getDouble(fixture.elevation(), child.id()));
            }
        }
    }

    @Test
    void overwriteRequiresConfirmationThenCollapsesDescendantValues() {
        var fixture = TestPlanetFactory.create();
        Cell parent = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 2);
        Cell child = parent.children().getFirst();
        Cell grandchild = child.children().getFirst();
        fixture.elevation().fillGapsAt(child, 700.0);
        fixture.elevation().fillGapsAt(grandchild, 900.0);

        var confirmation = fixture.elevation().overwriteAt(
            parent,
            250.0,
            false
        );

        assertEquals(FeatureMutationStatus.CONFIRMATION_REQUIRED, confirmation.status());
        assertEquals(900.0, fixture.elevation().valueAt(grandchild));

        var applied = fixture.elevation().overwriteAt(parent, 250.0, true);

        assertEquals(FeatureMutationStatus.APPLIED, applied.status());
        assertEquals(250.0, fixture.values().getDouble(fixture.elevation(), parent.id()));
        assertNull(fixture.values().getDouble(fixture.elevation(), child.id()));
        assertNull(fixture.values().getDouble(fixture.elevation(), grandchild.id()));
        assertEquals(250.0, fixture.elevation().valueAt(grandchild));
    }

    @Test
    void retrievalAndOverwriteSeeExactValuesAlreadyPresentInTheStore() {
        var fixture = TestPlanetFactory.create();
        Cell parent = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 2);
        Cell child = parent.children().getFirst();
        fixture.values().put(fixture.elevation(), child.id(), 700.0);

        double expected = 700.0 / parent.children().size();
        assertEquals(expected, fixture.elevation().valueAt(parent), 0.000_001);

        var confirmation = fixture.elevation().overwriteAt(parent, 300.0, false);
        assertEquals(FeatureMutationStatus.CONFIRMATION_REQUIRED, confirmation.status());

        fixture.elevation().overwriteAt(parent, 300.0, true);
        assertNull(fixture.values().getDouble(fixture.elevation(), child.id()));
        assertEquals(300.0, fixture.elevation().valueAt(child));
    }

    @Test
    void mixedWritesMaintainANonOverlappingExplicitFrontier() {
        var fixture = TestPlanetFactory.create();
        Cell parent = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 2);
        Cell child = parent.children().getFirst();
        Cell grandchild = child.children().getFirst();

        fixture.elevation().fillGapsAt(parent, 100.0);
        fixture.elevation().fillGapsAt(grandchild, 900.0);
        fixture.elevation().fillGapsAt(child, 400.0);

        var explicitIds = fixture.values().doubleValueCellIds(fixture.elevation());
        for (var first : explicitIds) {
            Cell firstCell = fixture.planet().cell(first);
            for (var second : explicitIds) {
                if (!first.equals(second)) assertFalse(firstCell.contains(second));
            }
        }
    }

    @Test
    void eraseRemovesOnlyTheExactValueAndEachCommandPublishesAtMostOneEvent() {
        var fixture = TestPlanetFactory.create();
        List<FeatureValuesChanged> events = new ArrayList<>();
        fixture.changes().subscribe(events::add);
        Cell cell = fixture.planet().cellAt(new PlanetLatLon(0.0, 0.0), 2);

        fixture.elevation().fillGapsAt(cell, 300.0);
        fixture.elevation().fillGapsAt(cell, 300.0);
        var confirmation = fixture.elevation().overwriteAt(
            cell.parent().orElseThrow(),
            500.0,
            false
        );
        var erased = fixture.elevation().eraseAt(cell);
        var secondErase = fixture.elevation().eraseAt(cell);

        assertEquals(FeatureMutationStatus.APPLIED, erased.status());
        assertEquals(FeatureMutationStatus.CONFIRMATION_REQUIRED, confirmation.status());
        assertEquals(FeatureMutationStatus.NO_CHANGE, secondErase.status());
        assertEquals(0.0, fixture.elevation().valueAt(cell));
        assertEquals(2, events.size());
    }
}
