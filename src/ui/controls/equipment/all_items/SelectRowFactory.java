package ui.controls.equipment.all_items;

import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import model.equipment.Equipment;

import java.util.function.Consumer;

public class SelectRowFactory implements javafx.util.Callback<javafx.scene.control.TreeTableView<ItemEntry>, javafx.scene.control.TreeTableRow<ItemEntry>> {
    private final Consumer<Equipment> handler;

    SelectRowFactory(Consumer<Equipment> handler) {
        this.handler = handler;
    }

    @Override
    public TreeTableRow<ItemEntry> call(TreeTableView<ItemEntry> item) {
        TreeTableRow<ItemEntry> call = new TreeTableRow<>();
        call.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                Equipment eq = call.getTreeItem().getValue().getItem();
                if(eq != null)
                    handler.accept(eq);
            }
        });
        return call;
    }
}
