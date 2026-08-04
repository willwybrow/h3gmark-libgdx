package dev.wycobar.hegmark.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FeatureChangeBus {
    private static final Logger LOG = Logger.getLogger(FeatureChangeBus.class.getName());
    private final List<FeatureChangeListener> listeners = new ArrayList<>();

    public void subscribe(FeatureChangeListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void publish(FeatureValuesChanged event) {
        for (FeatureChangeListener listener : List.copyOf(listeners)) {
            try {
                listener.featureValuesChanged(event);
            } catch (RuntimeException exception) {
                LOG.log(Level.SEVERE, "Feature change listener failed after mutation commit", exception);
            }
        }
    }
}
