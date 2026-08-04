package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.CellId;

import java.util.Optional;

public final class EditorState {
    private CellId selectedCell;
    private EditorTool tool = EditorTool.SELECT;
    private double paintElevationMeters = 500.0;
    private String message = "Select a cell in the live view";

    public Optional<CellId> selectedCell() {
        return Optional.ofNullable(selectedCell);
    }

    public void select(CellId cell) {
        selectedCell = cell;
        message = "Selected " + cell.asHexString();
    }

    public EditorTool tool() {
        return tool;
    }

    public void setTool(EditorTool tool) {
        this.tool = tool;
        message = tool.name().toLowerCase() + " tool active";
    }

    public double paintElevationMeters() {
        return paintElevationMeters;
    }

    public void setPaintElevationMeters(double paintElevationMeters) {
        this.paintElevationMeters = paintElevationMeters;
        message = "Paint value: " + Math.round(paintElevationMeters) + " m";
    }

    public String message() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
