package ui.controls.lists;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.*;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import ui.controls.lists.entries.ListEntry;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ObservableEntryList<T, U extends ListEntry<T>> extends AbstractEntryList<T, U> {
    protected final Collection<T> items;
    protected final Function<T, String> getCategory;
    protected final Function<T, TreeItem<U>> makeEntry;
    protected final Function<String, TreeItem<U>> makeLabelEntry;
    protected final Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns;
    private final List<Consumer<TreeItem<U>>> onInsert = new ArrayList<>();

    public static <T, U extends ListEntry<T>> ObservableEntryList<T, U> makeList(
            ObservableSet<T> items,
            BiConsumer<T, Integer> handler,
            Function<T, String> getCategory,
            Function<T, U> makeEntry,
            Function<String, U> makeLabelEntry,
            Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns) {
        ObservableEntryList<T, U> list = new ObservableEntryList<>(items, getCategory, makeEntry, makeLabelEntry, makeColumns);
        return makeList(list, handler);
    }

    public static <T, U extends ListEntry<T>> ObservableEntryList<T, U> makeList(
            ObservableList<T> items,
            BiConsumer<T, Integer> handler,
            Function<T, String> getCategory,
            Function<T, U> makeEntry,
            Function<String, U> makeLabelEntry,
            Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns) {
        ObservableEntryList<T, U> list = new ObservableEntryList<>(items, getCategory, makeEntry, makeLabelEntry, makeColumns);
        return makeList(list, handler);
    }

    private static <T, U extends ListEntry<T>> ObservableEntryList<T, U> makeList(
            ObservableEntryList<T, U> list,
            BiConsumer<T, Integer> handler) {
        list.construct((item, node, count)->handler.accept(item, count));
        list.getRoot().getChildren().sort(Comparator.comparing(o -> {
            try {
                return Integer.parseInt(o.getValue().toString());
            }catch (NumberFormatException e) {
                return 0;
            }
        }));
        return list;
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
        this.makeEntry = i->new TreeItem<>(makeEntry.apply(i));
        this.makeLabelEntry = i->new TreeItem<>(makeLabelEntry.apply(i));
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
                if(catNode.getValue().toString().equalsIgnoreCase(category)) {
                    insertCategoryChild(catNode, item);
                    return;
                }
            }
            TreeItem<U> cat = makeLabelEntry.apply(category);
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
                expand(getRoot(), getCategory);
                insert(item);
            }
        }
    }

    public void onInsert(Consumer<TreeItem<U>> listener) {
        onInsert.add(listener);
    }

    private void fireInsert(TreeItem<U> node) {
        for (Consumer<TreeItem<U>> listener : onInsert) {
            listener.accept(node);
        }
    }

    protected void insertCategoryChild(TreeItem<U> root, T item) {
        TreeItem<U> node = makeEntry.apply(item);
        root.getChildren().add(node);
        root.getChildren().sort(Comparator.comparing(TreeItem::getValue));
        fireInsert(node);
    }

    protected void expand(TreeItem<U> root, Function<T, String> grouping) {
        Map<String, TreeItem<U>> groupNodes = new HashMap<>();
        for (TreeItem<U> child : root.getChildren()) {
            T contents = child.getValue().getContents();
            String itemGroup = grouping.apply(contents);
            groupNodes.computeIfAbsent(itemGroup, makeLabelEntry)
                    .getChildren().add(deepCopy(child));
        }
        root.getChildren().setAll(groupNodes.values());
        root.getChildren().forEach(t->
                t.getChildren().sort(Comparator.comparing(TreeItem::getValue)));
        root.getChildren().sort(Comparator.comparing(TreeItem::getValue));
    }

    private void retract(T item) {
        if(multiCategory) {
            for (TreeItem<U> cat : getRoot().getChildren()) {
                if(cat.getValue().toString().equalsIgnoreCase(getCategory.apply(item))) {
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
            if(checkForCollapse(getRoot(), categoryRoot))
                multiCategory = false;
        }
    }

    protected boolean checkForCollapse(TreeItem<U> root, TreeItem<U> child) {
        if(root.getChildren().size() == 1) {
            ObservableList<TreeItem<U>> children = root.getChildren().get(0).getChildren();
            root.getChildren().clear();
            for (TreeItem<U> subChild : children) {
                root.getChildren().add(deepCopy(subChild));
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
