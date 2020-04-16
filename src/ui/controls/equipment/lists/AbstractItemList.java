package ui.controls.equipment.lists;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import model.equipment.Equipment;

import java.util.function.BiConsumer;

abstract class AbstractItemList extends TreeTableView<ItemEntry> {
    AbstractItemList(BiConsumer<Equipment, Integer> handler) {
        construct(handler);
    }

    AbstractItemList() {

    }

    abstract void addItems(TreeItem<ItemEntry> root);

    abstract void createColumns();

    void construct(BiConsumer<Equipment, Integer> handler) {
        this.setShowRoot(false);
        TreeItem<ItemEntry> root = new TreeItem<>(new ItemEntry("root"));
        this.setRoot(root);
        this.setRowFactory(new SelectRowFactory(handler));
        this.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        createColumns();
        addItems(root);
    }

    public void removeColumn(String name) {
        getColumns().removeIf(c->c.getText().toLowerCase().equals(name.toLowerCase()));
    }

    public void expandAll() {
        expand(getRoot());
    }

    private void expand(TreeItem<ItemEntry> root) {
        root.setExpanded(true);
        for (TreeItem<ItemEntry> child : root.getChildren()) {
            expand(child);
        }

    }
}
