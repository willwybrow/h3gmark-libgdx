package dev.wycobar.hegmark.render;

public record RgbColor(float red, float green, float blue) {
    public RgbColor {
        validate(red);
        validate(green);
        validate(blue);
    }

    private static void validate(float component) {
        if (!Float.isFinite(component) || component < 0.0f || component > 1.0f) {
            throw new IllegalArgumentException("Color components must be between zero and one");
        }
    }
}
