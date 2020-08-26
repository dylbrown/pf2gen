package ui.controls.lists.factories;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import ui.controls.lists.entries.SourceEntry;

public class ThreeStateCell extends TreeTableCell<SourceEntry, Boolean> {
    private CheckBox checkBox = new CheckBox();

    public ThreeStateCell(TreeTableColumn<SourceEntry, Boolean> list) {
        tableRowProperty().addListener((o, oldVal, newVal)->{
            if(newVal.getItem() != null)
                checkBox.indeterminateProperty().bindBidirectional(newVal.getItem().indeterminateProperty());
        });
        checkBox.selectedProperty().addListener((obs,wasSelected,isNowSelected) -> {
            int sel=getTreeTableRow().getIndex();
            TreeItem<SourceEntry> item = list.getTreeTableView().getSelectionModel().getModelItem(sel);
            update(item, isNowSelected);
        });
    }

    private void update(TreeItem<SourceEntry> item, Boolean value) {
        SourceEntry selectedItem = item.getValue();
        selectedItem.enabledProperty().set(value);
        selectedItem.indeterminateProperty().set(false);
        for (TreeItem<SourceEntry> child : item.getChildren()) {
            update(child, value);
        }
        updateParent(item.getParent(), value);
    }

    private void updateParent(TreeItem<SourceEntry> parent, Boolean value) {
        if(parent == null || parent.getValue() == null)
            return;
        boolean indeterminate = false;
        for (TreeItem<SourceEntry> child : parent.getChildren()) {
            if(child.getValue().isEnabled() != value) {
                indeterminate = true;
                break;
            }
        }
        parent.getValue().indeterminateProperty().set(indeterminate);
        if(!indeterminate)
            parent.getValue().enabledProperty().set(value);
    }

    @Override
    protected void updateItem(Boolean b, boolean empty) {
        super.updateItem(b, empty);
        if(empty){
            setGraphic(null);
        }else{
            if(b != null) {
                checkBox.setSelected(b);
            }
            checkBox.setIndeterminate(b == null);
            setGraphic(checkBox);
        }
    }
}
