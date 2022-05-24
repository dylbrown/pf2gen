package ui.controls.lists;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import model.util.TriConsumer;
import ui.controls.lists.entries.ListEntry;
import ui.controls.lists.factories.SelectRowFactory;

import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractEntryList<U, T extends ListEntry<U>> extends TreeTableView<T> {
    private final TreeItem<T> originalRoot = new TreeItem<>(null);
    private final TreeItem<T> filteredRoot = new TreeItem<>(null);
    private Predicate<T> lastPredicate = null;

    protected AbstractEntryList(TriConsumer<U, TreeItem<T>, Integer> handler) {
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
                checkSingleCategory(filteredRoot);
            }
            lastPredicate = newVal;
        });
    }

    private void checkSingleCategory(TreeItem<T> root) {
        TreeItem<T> curr = root;
        while(curr.getChildren().size() == 1) {
            TreeItem<T> temp = curr.getChildren().get(0);
            if(temp.getChildren().size() > 0) {
                curr = temp;
            } else break;
        }
        if(curr != root) {
            root.getChildren().setAll(
                    curr.getChildren().stream()
                            .map(AbstractEntryList::deepCopy)
                            .collect(Collectors.toList()));
        }
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

    protected void construct(TriConsumer<U, TreeItem<T>, Integer> handler) {
        this.setShowRoot(false);
        this.setRowFactory(new SelectRowFactory<>(Collections.singletonList((treeItem, i) -> {
            T ie = treeItem.getValue();
            if(ie != null && ie.getContents() != null)
                handler.accept(ie.getContents(), treeItem, i);
        })));
        this.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            T t = newVal.getValue();
            if(t != null && t.getContents() != null)
                handler.accept(t.getContents(), newVal, 1);
        });
        setRoot(originalRoot);
        this.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        createColumns();
        addItems(originalRoot);
    }

    public void removeColumn(String name) {
        getColumns().removeIf(c-> c.getText().equalsIgnoreCase(name));
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

    public static <U> TreeItem<U> deepCopy(TreeItem<U> original) {
        TreeItem<U> item = new TreeItem<>(original.getValue());
        for (TreeItem<U> child : original.getChildren()) {
            item.getChildren().add(deepCopy(child));
        }
        return item;
    }

    public TreeItem<T> findNode(U item) {
        return findNode(item, getRoot());
    }

    private TreeItem<T> findNode(U item, TreeItem<T> root) {
        if(root.getValue() != null && root.getValue().getContents() == item)
            return root;
        for (TreeItem<T> child : root.getChildren()) {
            TreeItem<T> node = findNode(item, child);
            if(node != null)
                return node;
        }
        return null;
    }
}
