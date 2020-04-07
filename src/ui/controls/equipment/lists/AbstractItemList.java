package ui.controls.equipment.lists;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import model.equipment.Equipment;

import java.util.function.Consumer;

abstract class AbstractItemList extends TreeTableView<ItemEntry> {
    AbstractItemList(Consumer<Equipment> handler) {
        construct(handler);
    }

    AbstractItemList() {

    }

    abstract void addItems(TreeItem<ItemEntry> root);

    abstract void createColumns();

    void construct(Consumer<Equipment> handler) {
        this.setShowRoot(false);
        TreeItem<ItemEntry> root = new TreeItem<>(new ItemEntry("root"));
        this.setRoot(root);
        this.setRowFactory(new SelectRowFactory(handler));
        this.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        createColumns();
        addItems(root);
    }
}
