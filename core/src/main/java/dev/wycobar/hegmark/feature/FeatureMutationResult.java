package dev.wycobar.hegmark.feature;

public record FeatureMutationResult(
    FeatureMutationStatus status,
    int changedValues,
    int removedValues
) {
    public FeatureMutationResult {
        if (changedValues < 0 || removedValues < 0) {
            throw new IllegalArgumentException("Mutation counts cannot be negative");
        }
    }

    public static FeatureMutationResult applied(int changedValues, int removedValues) {
        return new FeatureMutationResult(FeatureMutationStatus.APPLIED, changedValues, removedValues);
    }

    public static FeatureMutationResult noChange() {
        return new FeatureMutationResult(FeatureMutationStatus.NO_CHANGE, 0, 0);
    }

    public static FeatureMutationResult confirmationRequired(int removedValues) {
        return new FeatureMutationResult(FeatureMutationStatus.CONFIRMATION_REQUIRED, 0, removedValues);
    }

    public static FeatureMutationResult rejected() {
        return new FeatureMutationResult(FeatureMutationStatus.REJECTED, 0, 0);
    }
}
