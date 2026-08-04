package dev.wycobar.hegmark.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FeatureChangeBus {
    private final List<FeatureChangeListener> listeners = new ArrayList<>();

    public void subscribe(FeatureChangeListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void unsubscribe(FeatureChangeListener listener) {
        listeners.remove(listener);
    }

    public List<RuntimeException> publish(FeatureValuesChanged event) {
        List<RuntimeException> failures = new ArrayList<>();
        for (FeatureChangeListener listener : List.copyOf(listeners)) {
            try {
                listener.featureValuesChanged(event);
            } catch (RuntimeException exception) {
                failures.add(exception);
            }
        }
        return List.copyOf(failures);
    }
}
