package dev.wycobar.hegmark.planet;

import dev.wycobar.hegmark.feature.ComputedFeature;
import dev.wycobar.hegmark.feature.Feature;
import dev.wycobar.hegmark.feature.FeatureChangeBus;
import dev.wycobar.hegmark.feature.FeatureChangeListener;
import dev.wycobar.hegmark.feature.FeatureRegistry;
import dev.wycobar.hegmark.feature.FeatureValueSource;
import dev.wycobar.hegmark.feature.FeatureValueStore;
import dev.wycobar.hegmark.feature.FeatureMutationOperation;
import dev.wycobar.hegmark.feature.FeatureValuesChanged;
import dev.wycobar.hegmark.feature.InMemoryFeatureDisplayCache;
import dev.wycobar.hegmark.feature.ResolutionRange;
import dev.wycobar.hegmark.feature.ResolvedFeatureValue;
import dev.wycobar.hegmark.feature.StoredFeature;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Supplier;

public final class Planet implements FeatureChangeListener {
    private final PlanetModel definition;
    private final PlanetGrid grid;
    private final FeatureRegistry features;
    private final FeatureValueStore explicitValues;
    private final InMemoryFeatureDisplayCache displayCache;
    private final FeatureChangeBus changes;
    private final Set<EvaluationKey> activeEvaluations = new HashSet<>();
    private java.util.List<RuntimeException> lastNotificationFailures = java.util.List.of();

    public Planet(
        PlanetModel definition,
        PlanetGrid grid,
        FeatureRegistry features,
        FeatureValueStore explicitValues,
        InMemoryFeatureDisplayCache displayCache,
        FeatureChangeBus changes
    ) {
        this.definition = definition;
        this.grid = grid;
        this.features = features;
        this.explicitValues = explicitValues;
        this.displayCache = displayCache;
        this.changes = changes;
        changes.subscribe(this);
    }

    public PlanetModel definition() {
        return definition;
    }

    public Cell cell(CellId id) {
        return new Cell(this, id);
    }

    public Cell cellAt(PlanetLatLon coordinate, int resolution) {
        return cell(grid.cellAt(coordinate, resolution));
    }

    PlanetGrid grid() {
        return grid;
    }

    FeatureValueStore explicitValues() {
        return explicitValues;
    }

    public <T> int fillGaps(StoredFeature<T> feature, Collection<Cell> cells, T value) {
        LinkedHashSet<Cell> uniqueCells = new LinkedHashSet<>(cells);
        uniqueCells.forEach(cell -> validateEdit(feature, cell));
        Set<Cell> changed = new LinkedHashSet<>();
        for (Cell cell : uniqueCells) {
            Optional<T> previous = explicitValues.value(feature, cell.id());
            if (previous.isPresent() && previous.orElseThrow().equals(value)) continue;
            explicitValues.put(feature, cell.id(), value);
            changed.add(cell);
        }
        if (changed.isEmpty()) {
            lastNotificationFailures = java.util.List.of();
            return 0;
        }
        publish(new FeatureValuesChanged(
            feature,
            FeatureMutationOperation.FILL_GAPS,
            changed,
            Set.of()
        ));
        return changed.size();
    }

    public <T> int overwrite(StoredFeature<T> feature, Cell cell, T value) {
        validateEdit(feature, cell);
        Set<Cell> descendants = explicitValues.descendantValues(feature, cell).keySet().stream()
            .map(this::cell)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Optional<T> previous = explicitValues.value(feature, cell.id());
        boolean rootChanged = previous.isEmpty() || !previous.orElseThrow().equals(value);
        if (!rootChanged && descendants.isEmpty()) {
            lastNotificationFailures = java.util.List.of();
            return 0;
        }

        explicitValues.put(feature, cell.id(), value);
        descendants.forEach(descendant -> explicitValues.remove(feature, descendant.id()));
        publish(new FeatureValuesChanged(
            feature,
            FeatureMutationOperation.OVERWRITE,
            Set.of(cell),
            descendants
        ));
        return descendants.size();
    }

    public <T> boolean erase(StoredFeature<T> feature, Cell cell) {
        validateEdit(feature, cell);
        if (explicitValues.value(feature, cell.id()).isEmpty()) {
            lastNotificationFailures = java.util.List.of();
            return false;
        }
        explicitValues.remove(feature, cell.id());
        publish(new FeatureValuesChanged(
            feature,
            FeatureMutationOperation.ERASE,
            Set.of(cell),
            Set.of()
        ));
        return true;
    }

    public java.util.List<RuntimeException> lastNotificationFailures() {
        return lastNotificationFailures;
    }

    <T> T value(Cell cell, Feature<T> feature) {
        validate(feature, cell);
        if (feature instanceof StoredFeature<?> stored) {
            @SuppressWarnings("unchecked")
            StoredFeature<T> typed = (StoredFeature<T>) stored;
            return resolvedValue(cell, typed).displayValue();
        }
        if (feature instanceof ComputedFeature<?> computed) {
            return guarded(feature, cell, () -> feature.valueType().cast(computed.compute(cell)));
        }
        throw new IllegalArgumentException("Feature has no value behavior: " + feature.id());
    }

    <T> ResolvedFeatureValue<T> resolvedValue(Cell cell, StoredFeature<T> feature) {
        validate(feature, cell);
        ResolvedFeatureValue<T> effective = effectiveValue(cell, feature);
        long context = cacheContext(feature);
        Optional<T> cached = displayCache.value(feature, cell, context);
        if (cached.isPresent()) return withDisplay(effective, cached.orElseThrow());

        Optional<T> aggregate = guarded(feature, cell, () -> feature.aggregate(cell));
        if (aggregate.isEmpty()) return effective;
        T display = aggregate.orElseThrow();
        displayCache.put(feature, cell, context, display);
        return withDisplay(effective, display);
    }

    <T> Optional<T> explicitValue(Cell cell, StoredFeature<T> feature) {
        validateRegistered(feature, cell);
        return explicitValues.value(feature, cell.id());
    }

    boolean hasExplicitDescendant(Cell root, StoredFeature<?> feature) {
        validateRegistered(feature, root);
        return !explicitValues.descendantValues(feature, root).isEmpty();
    }

    int explicitDescendantCount(Cell root, StoredFeature<?> feature) {
        validateRegistered(feature, root);
        return explicitValues.descendantValues(feature, root).size();
    }

    @Override
    public void featureValuesChanged(FeatureValuesChanged event) {
        event.changedCells().forEach(cell -> displayCache.removeSubtree(event.feature(), cell));
        Set<Cell> affected = event.feature().affectedAggregationCells(event);
        affected.forEach(cell -> displayCache.remove(event.feature(), cell));
        affected.stream()
            .sorted(java.util.Comparator.comparingInt(Cell::resolution).reversed())
            .forEach(cell -> refresh(event.feature(), cell));
    }

    private <T> ResolvedFeatureValue<T> effectiveValue(Cell requestedCell, StoredFeature<T> feature) {
        ResolutionRange settable = feature.settableRange().orElseThrow();
        Cell applicableCell = requestedCell.resolution() > settable.maximum()
            ? requestedCell.parent(settable.maximum())
            : requestedCell;

        if (requestedCell.resolution() >= settable.minimum()) {
            Cell candidate = applicableCell;
            while (candidate.resolution() >= settable.minimum()) {
                Optional<T> explicit = explicitValues.value(feature, candidate.id());
                if (explicit.isPresent()) {
                    T value = explicit.orElseThrow();
                    return resolved(requestedCell, feature, value, FeatureValueSource.EXPLICIT, candidate);
                }
                if (candidate.resolution() == settable.minimum()) break;
                candidate = candidate.parent().orElseThrow();
            }
        }

        T generated = guarded(feature, applicableCell, () -> feature.generatedValue(applicableCell));
        return resolved(requestedCell, feature, generated, FeatureValueSource.GENERATED, applicableCell);
    }

    private <T> ResolvedFeatureValue<T> resolved(
        Cell requestedCell,
        StoredFeature<T> feature,
        T value,
        FeatureValueSource source,
        Cell sourceCell
    ) {
        return new ResolvedFeatureValue<>(
            true,
            feature.isSettableAt(requestedCell.resolution()),
            explicitValues.value(feature, requestedCell.id()),
            value,
            value,
            source,
            source,
            Optional.of(sourceCell)
        );
    }

    private <T> ResolvedFeatureValue<T> withDisplay(ResolvedFeatureValue<T> effective, T display) {
        return new ResolvedFeatureValue<>(
            effective.applicable(),
            effective.directlyEditable(),
            effective.storedValue(),
            effective.effectiveValue(),
            display,
            effective.effectiveSource(),
            FeatureValueSource.AGGREGATED,
            effective.sourceCell()
        );
    }

    private void validate(Feature<?> feature, Cell cell) {
        validateRegistered(feature, cell);
        if (!feature.isViewableAt(cell.resolution())) {
            throw new IllegalArgumentException(feature.name() + " is not viewable at resolution " + cell.resolution());
        }
    }

    private void validateRegistered(Feature<?> feature, Cell cell) {
        if (cell.planet() != this) throw new IllegalArgumentException("Cell belongs to another planet");
        if (features.feature(feature.id()) != feature) {
            throw new IllegalArgumentException("Feature instance is not registered: " + feature.id());
        }
    }

    private void validateEdit(StoredFeature<?> feature, Cell cell) {
        validate(feature, cell);
        if (!feature.isSettableAt(cell.resolution())) {
            throw new IllegalArgumentException(feature.name() + " is not editable at resolution " + cell.resolution());
        }
    }

    private long cacheContext(StoredFeature<?> feature) {
        return definition.worldSeed()
            ^ Long.rotateLeft(feature.id().hashCode(), 21)
            ^ Long.rotateLeft(feature.algorithmVersion(), 42);
    }

    private <T> T guarded(Feature<?> feature, Cell cell, Supplier<T> operation) {
        EvaluationKey key = new EvaluationKey(feature, cell.id());
        if (!activeEvaluations.add(key)) {
            throw new IllegalStateException("Cyclic feature evaluation for " + feature.id() + " at " + cell.id().asHexString());
        }
        try {
            return operation.get();
        } finally {
            activeEvaluations.remove(key);
        }
    }

    private void refresh(Feature<?> feature, Cell cell) {
        valueUnchecked(cell, feature);
    }

    private void publish(FeatureValuesChanged event) {
        lastNotificationFailures = changes.publish(event);
    }

    private <T> T valueUnchecked(Cell cell, Feature<T> feature) {
        return value(cell, feature);
    }

    private record EvaluationKey(Feature<?> feature, CellId cell) {
    }
}
