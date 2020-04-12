package ui.controls.equipment.lists;

import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import model.equipment.Equipment;

import java.util.function.BiConsumer;

class SelectRowFactory implements javafx.util.Callback<javafx.scene.control.TreeTableView<ItemEntry>, javafx.scene.control.TreeTableRow<ItemEntry>> {
    private final BiConsumer<Equipment, Integer> handler;

    SelectRowFactory(BiConsumer<Equipment, Integer> handler) {
        this.handler = handler;
    }

    @Override
    public TreeTableRow<ItemEntry> call(TreeTableView<ItemEntry> item) {
        TreeTableRow<ItemEntry> call = new TreeTableRow<>();
        call.setOnMouseClicked(event -> {
            if(call.getTreeItem() == null) return;
            Equipment eq = call.getTreeItem().getValue().getItem();
            if(eq != null)
                handler.accept(eq, event.getClickCount());
        });
        return call;
    }
}
