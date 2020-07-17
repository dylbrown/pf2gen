package ui.controls.lists.factories;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import ui.controls.lists.entries.TreeTableEntry;

public class TreeCellFactory<T extends TreeTableEntry> implements Callback<TreeTableColumn.CellDataFeatures<T, String>, ObservableValue<String>> {
    private final String propertyName;

    public TreeCellFactory(String property) {
        this.propertyName = property;
    }

    @Override
    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<T, String> f) {
        return f.getValue().getValue().get(propertyName);
    }
}
