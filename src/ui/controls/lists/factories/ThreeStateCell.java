package ui.controls.lists.factories;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import ui.controls.lists.ThreeState;
import ui.controls.lists.entries.SourceEntry;

public class ThreeStateCell extends TreeTableCell<SourceEntry, ThreeState> {
    private CheckBox checkBox = new CheckBox();

    public ThreeStateCell(TreeTableColumn<SourceEntry, ThreeState> list) {
        checkBox.selectedProperty().addListener((obs,wasSelected,isNowSelected) -> {
            int sel=getTreeTableRow().getIndex();
            TreeItem<SourceEntry> item = list.getTreeTableView().getSelectionModel().getModelItem(sel);
            update(item, isNowSelected);
        });
    }

    private void update(TreeItem<SourceEntry> item, Boolean value) {
        SourceEntry selectedItem = item.getValue();
        if(selectedItem.isLocked())
            return;
        if(value)
            selectedItem.stateProperty().set(ThreeState.True);
        else
            selectedItem.stateProperty().set(ThreeState.False);
        checkBox.setIndeterminate(false);
        for (TreeItem<SourceEntry> child : item.getChildren()) {
            update(child, value);
        }
        updateParent(item.getParent(), value);
    }

    public static void updateParent(TreeItem<SourceEntry> parent, Boolean value) {
        if(parent == null || parent.getValue() == null)
            return;
        boolean indeterminate = false;
        for (TreeItem<SourceEntry> child : parent.getChildren()) {
            if(child.getValue().getState().asBoolean() != value) {
                indeterminate = true;
                break;
            }
        }
        if(!parent.getValue().isLocked()) {
            if(indeterminate)
                parent.getValue().stateProperty().set(ThreeState.Indeterminate);
            else
                parent.getValue().stateProperty().set(ThreeState.valueOf(value));
        }
    }

    @Override
    protected void updateItem(ThreeState s, boolean empty) {
        super.updateItem(s, empty);
        if(empty){
            setGraphic(null);
        }else{
            if(s.asBoolean() != null) {
                checkBox.setSelected(s.asBoolean());
            }
            checkBox.setIndeterminate(s.asBoolean() == null);
            setGraphic(checkBox);
            checkBox.setDisable(s == ThreeState.LockedTrue);
        }
    }
}
