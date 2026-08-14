package dev.wycobar.hegmark.editor;

public record UiButton(
    UiAction action,
    String targetId,
    String label,
    UiRect bounds,
    boolean active,
    boolean enabled
) {
    public UiButton(UiAction action, String label, UiRect bounds, boolean active, boolean enabled) {
        this(action, null, label, bounds, active, enabled);
    }
}
