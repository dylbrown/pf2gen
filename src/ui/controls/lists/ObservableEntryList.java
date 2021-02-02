package ui.controls.lists;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.*;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import ui.controls.lists.entries.ListEntry;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ObservableEntryList<T, U extends ListEntry<T>> extends AbstractEntryList<T, U> {
    protected final Collection<T> items;
    protected final Function<T, String> getCategory;
    protected final Function<T, U> makeEntry;
    protected final Function<String, U> makeLabelEntry;
    protected final Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns;

    public static <T, U extends ListEntry<T>, X> ObservableEntryList<T, U> makeList(ObservableMap<X, T> items,
                                                                                 BiConsumer<T, Integer> handler,
                                                                                 Function<T, String> getCategory,
                                                                                 Function<T, U> makeEntry,
                                                                                 Function<String, U> makeLabelEntry,
                                                                                 Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns) {
        ObservableEntryList<T, U> list = new ObservableEntryList<>(items, getCategory, makeEntry, makeLabelEntry, makeColumns);
        return makeList(list, handler);
    }

    public static <T, U extends ListEntry<T>> ObservableEntryList<T, U> makeList(ObservableSet<T> items,
                                                                                 BiConsumer<T, Integer> handler,
                                                                                 Function<T, String> getCategory,
                                                                                 Function<T, U> makeEntry,
                                                                                 Function<String, U> makeLabelEntry,
                                                                                 Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns) {
        ObservableEntryList<T, U> list = new ObservableEntryList<>(items, getCategory, makeEntry, makeLabelEntry, makeColumns);
        return makeList(list, handler);
    }

    public static <T, U extends ListEntry<T>> ObservableEntryList<T, U> makeList(ObservableList<T> items,
                           BiConsumer<T, Integer> handler,
                           Function<T, String> getCategory,
                           Function<T, U> makeEntry,
                           Function<String, U> makeLabelEntry,
                           Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns) {
        ObservableEntryList<T, U> list = new ObservableEntryList<>(items, getCategory, makeEntry, makeLabelEntry, makeColumns);
        return makeList(list, handler);
    }

    private static <T, U extends ListEntry<T>> ObservableEntryList<T, U> makeList(ObservableEntryList<T, U> list,
                                                                                  BiConsumer<T, Integer> handler) {
        list.construct(handler);
        list.getRoot().getChildren().sort(Comparator.comparing(o -> {
            try {
                return Integer.parseInt(o.getValue().toString());
            }catch (NumberFormatException e) {
                return 0;
            }
        }));
        return list;
    }

    private <X> ObservableEntryList(ObservableMap<X, T> items,
                                      Function<T, String> getCategory,
                                      Function<T, U> makeEntry,
                                      Function<String, U> makeLabelEntry,
                                      Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns) {
        this(getCategory, makeEntry, makeLabelEntry, makeColumns, items.values());
        items.addListener((MapChangeListener<X, T>)  c-> {
            if(c.wasAdded())
                insert(c.getValueAdded());
            if(c.wasRemoved())
                retract(c.getValueRemoved());
        });
    }

    private ObservableEntryList(ObservableSet<T> items,
                                  Function<T, String> getCategory,
                                  Function<T, U> makeEntry,
                                  Function<String, U> makeLabelEntry,
                                  Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns) {
        this(getCategory, makeEntry, makeLabelEntry, makeColumns, items);
        items.addListener((SetChangeListener<T>)  c-> {
            if(c.wasAdded())
                insert(c.getElementAdded());
            if(c.wasRemoved())
                retract(c.getElementRemoved());
        });
    }

    protected ObservableEntryList(ObservableList<T> items,
                                       Function<T, String> getCategory,
                                       Function<T, U> makeEntry,
                                       Function<String, U> makeLabelEntry,
                                       Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns) {
        this(getCategory, makeEntry, makeLabelEntry, makeColumns, items);
        items.addListener((ListChangeListener<T>) c -> {
            while(c.next()) {
                if(!c.wasPermutated() && !c.wasUpdated()) {
                    for (T item : c.getRemoved()) {
                        retract(item);
                    }
                    for (T item : c.getAddedSubList()) {
                        insert(item);
                    }
                }
            }
        });
    }

    private ObservableEntryList(Function<T, String> getCategory,
                                Function<T, U> makeEntry,
                                Function<String, U> makeLabelEntry,
                                Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns,
                                Collection<T> items) {
        this.getCategory = getCategory;
        this.makeEntry = makeEntry;
        this.makeLabelEntry = makeLabelEntry;
        this.makeColumns = makeColumns;
        this.items = items;
    }

    protected boolean multiCategory = false;
    protected final Set<String> categoryStrings = new HashSet<>();
    @Override
    protected void addItems(TreeItem<U> root) {
        for (T item : items) {
            insert(item);
        }
    }

    private void insert(T item) {
        String category = getCategory.apply(item);
        if(multiCategory) {
            for (TreeItem<U> catNode : getRoot().getChildren()) {
                if(catNode.getValue().toString().equals(category)) {
                    insertCategoryChild(catNode, item);
                    return;
                }
            }
            TreeItem<U> cat = new TreeItem<>(makeLabelEntry.apply(category));
            categoryStrings.add(category);
            getRoot().getChildren().add(cat);
            insertCategoryChild(cat, item);
            getRoot().getChildren().sort(Comparator.comparing(TreeItem::getValue));
        }else{
            if(categoryStrings.size() == 0 || categoryStrings.contains(category)) {
                categoryStrings.add(category);
                insertCategoryChild(getRoot(), item);
            }else{
                multiCategory = true;
                expand(getRoot(), categoryStrings.iterator().next());
                insert(item);
            }
        }
    }

    protected void insertCategoryChild(TreeItem<U> root, T item) {
        root.getChildren().add(new TreeItem<>(makeEntry.apply(item)));
        root.getChildren().sort(Comparator.comparing(TreeItem::getValue));
    }

    protected void expand(TreeItem<U> root, String label) {
        TreeItem<U> group = new TreeItem<>(makeLabelEntry.apply(label));
        for (TreeItem<U> child : root.getChildren()) {
            group.getChildren().add(deepCopy(child));
        }
        root.getChildren().clear();
        root.getChildren().add(group);
        group.getChildren().sort(Comparator.comparing(TreeItem::getValue));
    }

    private void retract(T item) {
        if(multiCategory) {
            for (TreeItem<U> cat : getRoot().getChildren()) {
                if(cat.getValue().toString().equals(getCategory.apply(item))) {
                    retractCategoryChild(cat, item);
                    break;
                }
            }
        }else{
            retractCategoryChild(getRoot(), item);
        }
    }

    protected void retractCategoryChild(TreeItem<U> categoryRoot, T item) {
        categoryRoot.getChildren().removeIf(ti->item.equals(ti.getValue().getContents()));
        if(categoryRoot.getChildren().size() == 0) {
            getRoot().getChildren().remove(categoryRoot);
            categoryStrings.remove(getCategory.apply(item));
            if(checkForCollapse(getRoot()))
                multiCategory = false;
        }
    }

    protected boolean checkForCollapse(TreeItem<U> root) {
        if(root.getChildren().size() == 1) {
            ObservableList<TreeItem<U>> children = root.getChildren().get(0).getChildren();
            root.getChildren().clear();
            for (TreeItem<U> child : children) {
                root.getChildren().add(deepCopy(child));
            }
            return true;
        }
        return false;
    }

    @Override
    protected void createColumns() {
        this.getColumns().addAll(makeColumns.apply(this.widthProperty()));
    }
}
