package ui.controls.lists;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import model.data_managers.sources.Source;
import ui.controls.lists.entries.ListEntry;
import ui.controls.lists.factories.SelectRowFactory;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractEntryList<U, T extends ListEntry<U>> extends TreeTableView<T> {
    private final TreeItem<T> originalRoot = new TreeItem<>(null);
    private final TreeItem<T> filteredRoot = new TreeItem<>(null);
    private Predicate<T> lastPredicate = null;

    protected AbstractEntryList(BiConsumer<U, Integer> handler) {
        construct(handler);
    }

    public void setFilter(ReadOnlyObjectProperty<Predicate<T>> filter) {
        filter.addListener((o, oldVal, newVal)->{
            if(newVal == null){
                setRoot(originalRoot);
            } else if(newVal == lastPredicate){
                setRoot(filteredRoot);
            } else {
                setRoot(filteredRoot);
                filteredRoot.getChildren().clear();
                updateVisible(originalRoot, ()->filteredRoot, newVal);
            }
            lastPredicate = newVal;
        });
    }

    private void updateVisible(TreeItem<T> source,
                               Supplier<TreeItem<T>> dest,
                               Predicate<T> predicate) {
        for (TreeItem<T> child : source.getChildren()) {
            Property<TreeItem<T>> parent = new SimpleObjectProperty<>();
            if(predicate.test(child.getValue())) {
                addToFilter(child, dest.get());
            }else{
                updateVisible(child, ()->{
                    if(parent.getValue() == null) {
                        TreeItem<T> grandparent = dest.get();
                        parent.setValue(new TreeItem<>(child.getValue()));
                        grandparent.getChildren().add(parent.getValue());
                    }
                    return parent.getValue();
                }, predicate);
            }
        }

    }

    private void addToFilter(TreeItem<T> child, TreeItem<T> dest) {
        TreeItem<T> childInDest = new TreeItem<>(child.getValue());
        dest.getChildren().add(childInDest);
        for (TreeItem<T> grandchild : child.getChildren()) {
            addToFilter(grandchild, childInDest);
        }
    }

    protected AbstractEntryList() {

    }

    protected abstract void addItems(TreeItem<T> root);

    protected abstract void createColumns();

    protected void construct(BiConsumer<U, Integer> handler) {
        this.setShowRoot(false);
        this.setRowFactory(new SelectRowFactory<>(Collections.singletonList((treeItem, i) -> {
            T ie = treeItem.getValue();
            if(ie != null && ie.getContents() != null)
                handler.accept(ie.getContents(), i);
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

    private void expand(TreeItem<T> root) {
        root.setExpanded(true);
        for (TreeItem<T> child : root.getChildren()) {
            expand(child);
        }

    }
}
