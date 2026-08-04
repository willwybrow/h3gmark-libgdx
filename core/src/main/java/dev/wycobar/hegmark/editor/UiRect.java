package dev.wycobar.hegmark.editor;

public record UiRect(float x, float y, float width, float height) {
    public boolean contains(float screenX, float screenY) {
        return screenX >= x && screenX <= x + width && screenY >= y && screenY <= y + height;
    }
}
