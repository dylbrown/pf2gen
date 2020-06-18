package ui.controls.lists;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import model.equipment.Equipment;
import ui.controls.lists.entries.ItemEntry;

import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

abstract class AbstractItemList extends TreeTableView<ItemEntry> {
    private final TreeItem<ItemEntry> originalRoot = new TreeItem<>(new ItemEntry("root"));
    private final TreeItem<ItemEntry> filteredRoot = new TreeItem<>(new ItemEntry("root"));
    AbstractItemList(BiConsumer<Equipment, Integer> handler) {
        construct(handler);
    }

    public void setFilter(ReadOnlyObjectProperty<Predicate<ItemEntry>> filter) {
        filter.addListener((o, oldVal, newVal)->{
            if(newVal == null){
                setRoot(originalRoot);
            } else {
                setRoot(filteredRoot);
                filteredRoot.getChildren().clear();
                updateVisible(originalRoot, ()->filteredRoot, newVal);
            }
        });
    }

    private void updateVisible(TreeItem<ItemEntry> source,
                               Supplier<TreeItem<ItemEntry>> dest,
                               Predicate<ItemEntry> predicate) {
        for (TreeItem<ItemEntry> child : source.getChildren()) {
            Property<TreeItem<ItemEntry>> parent = new SimpleObjectProperty<>();
            if(predicate.test(child.getValue())) {
                addToFilter(child, dest.get());
            }else{
                updateVisible(child, ()->{
                    if(parent.getValue() == null) {
                        TreeItem<ItemEntry> grandparent = dest.get();
                        parent.setValue(new TreeItem<>(child.getValue()));
                        grandparent.getChildren().add(parent.getValue());
                    }
                    return parent.getValue();
                }, predicate);
            }
        }

    }

    private void addToFilter(TreeItem<ItemEntry> child, TreeItem<ItemEntry> dest) {
        TreeItem<ItemEntry> childInDest = new TreeItem<>(child.getValue());
        dest.getChildren().add(childInDest);
        for (TreeItem<ItemEntry> grandchild : child.getChildren()) {
            addToFilter(grandchild, childInDest);
        }
    }

    AbstractItemList() {

    }

    abstract void addItems(TreeItem<ItemEntry> root);

    abstract void createColumns();

    void construct(BiConsumer<Equipment, Integer> handler) {
        this.setShowRoot(false);
        this.setRowFactory(new SelectRowFactory<>(Collections.singletonList((treeItem, i) -> {
            ItemEntry ie = treeItem.getValue();
            if(ie != null && ie.getItem() != null)
                handler.accept(ie.getItem(), i);
        })));
        setRoot(originalRoot);
        this.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        createColumns();
        addItems(originalRoot);
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
