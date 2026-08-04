package dev.wycobar.hegmark.editor;

public final class EditorLayout {
    private int width = 1;
    private int height = 1;
    private int liveWidth = 1;
    private int panelWidth = 1;

    public void resize(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        panelWidth = Math.min(420, Math.max(320, this.width * 35 / 100));
        if (this.width < 640) panelWidth = Math.max(240, this.width / 2);
        panelWidth = Math.min(panelWidth, Math.max(1, this.width - 1));
        liveWidth = Math.max(1, this.width - panelWidth);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int liveWidth() {
        return liveWidth;
    }

    public int panelWidth() {
        return panelWidth;
    }

    public float panelX() {
        return liveWidth;
    }
}
