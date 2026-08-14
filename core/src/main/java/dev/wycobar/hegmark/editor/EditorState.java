package dev.wycobar.hegmark.editor;

import dev.wycobar.hegmark.planet.Cell;

import java.util.Optional;

public final class EditorState {
    private Cell selectedCell;
    private EditorTool tool = EditorTool.SELECT;
    private double paintElevationMeters = 500.0;
    private String activeRendererId;
    private String message = "Select a cell in the live view";
    private Cell pendingOverwriteCell;
    private double pendingOverwriteValue;

    public Optional<Cell> selectedCell() {
        return Optional.ofNullable(selectedCell);
    }

    public void select(Cell cell) {
        if (pendingOverwriteCell != null && !pendingOverwriteCell.equals(cell)) clearOverwriteConfirmation();
        selectedCell = cell;
        message = "Selected " + cell.id().asHexString();
    }

    public EditorTool tool() {
        return tool;
    }

    public void setTool(EditorTool tool) {
        this.tool = tool;
        clearOverwriteConfirmation();
        message = tool.name().toLowerCase() + " tool active";
    }

    public double paintElevationMeters() {
        return paintElevationMeters;
    }

    public String activeRendererId() {
        return activeRendererId;
    }

    public boolean selectRenderer(String rendererId) {
        if (rendererId.equals(activeRendererId)) return false;
        activeRendererId = rendererId;
        clearOverwriteConfirmation();
        message = "Viewing " + rendererId;
        return true;
    }

    public void setPaintElevationMeters(double paintElevationMeters) {
        this.paintElevationMeters = paintElevationMeters;
        clearOverwriteConfirmation();
        message = "Paint value: " + Math.round(paintElevationMeters) + " m";
    }

    public String message() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isOverwriteConfirmed(Cell cell) {
        return cell.equals(pendingOverwriteCell)
            && Double.compare(paintElevationMeters, pendingOverwriteValue) == 0;
    }

    public void requestOverwriteConfirmation(Cell cell, int descendantCount) {
        pendingOverwriteCell = cell;
        pendingOverwriteValue = paintElevationMeters;
        message = "Overwrite removes " + descendantCount + " descendant values; click cell again to confirm";
    }

    public void clearOverwriteConfirmation() {
        pendingOverwriteCell = null;
    }
}
