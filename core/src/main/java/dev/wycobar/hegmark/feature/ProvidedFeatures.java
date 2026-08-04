package dev.wycobar.hegmark.feature;

import java.util.List;

public interface ProvidedFeatures {
    List<? extends ComputedFeature<?>> providedFeatures();
}
