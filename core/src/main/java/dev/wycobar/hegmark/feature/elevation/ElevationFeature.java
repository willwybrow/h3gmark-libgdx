package dev.wycobar.hegmark.feature.elevation;

import dev.wycobar.hegmark.feature.ComputedFeature;
import dev.wycobar.hegmark.feature.FeatureChangeBus;
import dev.wycobar.hegmark.feature.FeatureMutationResult;
import dev.wycobar.hegmark.feature.FeatureValueStore;
import dev.wycobar.hegmark.feature.FeatureValuesChanged;
import dev.wycobar.hegmark.feature.ResolutionRange;
import dev.wycobar.hegmark.feature.StoredFeature;
import dev.wycobar.hegmark.planet.Cell;
import dev.wycobar.hegmark.planet.CellId;
import dev.wycobar.hegmark.planet.Layer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ElevationFeature extends StoredFeature<Double> {
    private static final double DEFAULT_ELEVATION_METERS = 0.0;
    private static final ResolutionRange VIEWABLE = Layer.anywhere();
    private static final ResolutionRange SETTABLE = Layer.anywhere();

    private final LandFeature landFeature;
    private final FeatureChangeBus changes;

    public ElevationFeature(FeatureValueStore featureValueStore, FeatureChangeBus changes) {
        super(featureValueStore);
        this.changes = changes;
        landFeature = new LandFeature(this);
    }

    @Override
    public String id() {
        return "elevation";
    }

    @Override
    public String name() {
        return "Elevation";
    }

    @Override
    public Double valueAt(Cell cell) {
        Double exactValue = explicitValueAt(cell);
        if (exactValue != null) return exactValue;

        Optional<Cell> ancestor = explicitAncestorOf(cell);
        if (ancestor.isPresent()) return explicitValueAt(ancestor.orElseThrow());

        if (!hasExplicitDescendant(cell)) return DEFAULT_ELEVATION_METERS;
        return cell.children().stream()
            .mapToDouble(this::valueAt)
            .average()
            .orElse(DEFAULT_ELEVATION_METERS);
    }

    @Override
    public FeatureMutationResult fillGapsAt(Cell cell, Double value) {
        if (!canWrite(cell, value)) return FeatureMutationResult.rejected();
        Mutation mutation = new Mutation();
        Optional<Cell> explicitAncestor = explicitAncestorOf(cell);
        if (explicitAncestor.isPresent()) {
            Cell ancestor = explicitAncestor.orElseThrow();
            double inheritedValue = explicitValueAt(ancestor);
            splitAncestorWrite(ancestor, cell, inheritedValue, value, mutation);
        } else {
            fillGaps(cell, value, mutation);
        }
        return complete(mutation);
    }

    @Override
    public FeatureMutationResult overwriteAt(Cell cell, Double value, boolean destructiveEditConfirmed) {
        if (!canWrite(cell, value)) return FeatureMutationResult.rejected();
        int descendantCount = explicitDescendantCount(cell);
        if (descendantCount > 0 && !destructiveEditConfirmed) {
            return FeatureMutationResult.confirmationRequired(descendantCount);
        }

        Mutation mutation = new Mutation();
        Optional<Cell> explicitAncestor = explicitAncestorOf(cell);
        if (explicitAncestor.isPresent()) {
            Cell ancestor = explicitAncestor.orElseThrow();
            double inheritedValue = explicitValueAt(ancestor);
            splitAncestorWrite(ancestor, cell, inheritedValue, value, mutation);
        } else {
            removeExplicitDescendants(cell, mutation);
            putExplicit(cell, value, mutation);
        }
        return complete(mutation);
    }

    @Override
    public FeatureMutationResult eraseAt(Cell cell) {
        if (!isSettableAt(cell.layer())) return FeatureMutationResult.rejected();
        Mutation mutation = new Mutation();
        removeExplicit(cell, mutation);
        return complete(mutation);
    }

    @Override
    public ResolutionRange viewableRange() {
        return VIEWABLE;
    }

    @Override
    public Optional<ResolutionRange> settableRange() {
        return Optional.of(SETTABLE);
    }

    @Override
    protected Set<ComputedFeature<?>> additionalFeatures() {
        return Set.of(landFeature);
    }

    private Double explicitValueAt(Cell cell) {
        return featureValueStore.getDouble(this, cell.id());
    }

    private Optional<Cell> explicitAncestorOf(Cell cell) {
        Optional<Cell> ancestor = cell.parent();
        while (ancestor.isPresent()) {
            Cell candidate = ancestor.orElseThrow();
            if (explicitValueAt(candidate) != null) return Optional.of(candidate);
            ancestor = candidate.parent();
        }
        return Optional.empty();
    }

    private boolean hasExplicitDescendant(Cell cell) {
        return explicitCellIds().stream().anyMatch(cell::strictlyContains);
    }

    private int explicitDescendantCount(Cell cell) {
        return (int) explicitCellIds().stream().filter(cell::strictlyContains).count();
    }

    private void splitAncestorWrite(
        Cell ancestor,
        Cell target,
        double inheritedValue,
        double newValue,
        Mutation mutation
    ) {
        removeExplicit(ancestor, mutation);
        Cell branch = ancestor;
        while (!branch.equals(target)) {
            List<Cell> children = branch.children();
            Cell next = children.stream()
                .filter(child -> child.equals(target) || child.contains(target.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Target is not within its explicit ancestor"));
            for (Cell child : children) {
                if (!child.equals(next)) putExplicit(child, inheritedValue, mutation);
            }
            branch = next;
        }
        putExplicit(target, newValue, mutation);
    }

    private void fillGaps(Cell cell, double value, Mutation mutation) {
        if (explicitValueAt(cell) != null) {
            putExplicit(cell, value, mutation);
            return;
        }
        if (!hasExplicitDescendant(cell)) {
            putExplicit(cell, value, mutation);
            return;
        }
        for (Cell child : cell.children()) {
            if (explicitValueAt(child) != null) continue;
            fillGaps(child, value, mutation);
        }
    }

    private void removeExplicitDescendants(Cell cell, Mutation mutation) {
        List<CellId> descendants = explicitCellIds().stream().filter(cell::strictlyContains).toList();
        for (CellId descendant : descendants) {
            removeExplicit(cell.planet().cell(descendant), mutation);
        }
    }

    private void putExplicit(Cell cell, double value, Mutation mutation) {
        Double previous = featureValueStore.getDouble(this, cell.id());
        if (previous != null && Double.compare(previous, value) == 0) return;
        featureValueStore.put(this, cell.id(), value);
        mutation.changedCells.add(cell);
    }

    private void removeExplicit(Cell cell, Mutation mutation) {
        if (!featureValueStore.remove(this, cell.id())) return;
        mutation.removedCells.add(cell);
    }

    private Set<CellId> explicitCellIds() {
        return featureValueStore.doubleValueCellIds(this);
    }

    private boolean canWrite(Cell cell, Double value) {
        return isSettableAt(cell.layer()) && value != null && Double.isFinite(value);
    }

    private FeatureMutationResult complete(Mutation mutation) {
        if (mutation.changedCells.isEmpty() && mutation.removedCells.isEmpty()) {
            return FeatureMutationResult.noChange();
        }
        changes.publish(new FeatureValuesChanged(this, mutation.changedCells, mutation.removedCells));
        return FeatureMutationResult.applied(mutation.changedCells.size(), mutation.removedCells.size());
    }

    private static final class Mutation {
        private final Set<Cell> changedCells = new LinkedHashSet<>();
        private final Set<Cell> removedCells = new LinkedHashSet<>();
    }
}
