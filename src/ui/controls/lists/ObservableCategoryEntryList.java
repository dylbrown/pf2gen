package ui.controls.lists;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import ui.controls.lists.entries.ListEntry;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ObservableCategoryEntryList<T, U extends ListEntry<T>> extends AbstractEntryList<T, U> {
    private final ObservableList<T> items;
    private final Function<T, String> getCategory;
    private final Function<T, String> getSubCategory;
    private final Function<T, U> makeEntry;
    private final Function<String, U> makeLabelEntry;
    private final Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, String>>> makeColumns;

    public ObservableCategoryEntryList(ObservableList<T> items,
                                       BiConsumer<T, Integer> handler,
                                       Function<T, String> getCategory,
                                       Function<T, String> getSubCategory,
                                       Function<T, U> makeEntry,
                                        Function<String, U> makeLabelEntry,
                                       Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, String>>> makeColumns) {
        super();
        this.items = items;
        this.getCategory = getCategory;
        this.getSubCategory = getSubCategory;
        this.makeEntry = makeEntry;
        this.makeLabelEntry = makeLabelEntry;
        this.makeColumns = makeColumns;
        this.items.addListener((ListChangeListener<T>) c -> {
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
        super.construct(handler);
    }

    private boolean multiCategory = false;
    private final Set<String> categoryStrings = new HashSet<>();
    @Override
    protected void addItems(TreeItem<U> root) {
        Map<String, TreeItem<U>> categories = new TreeMap<>();
        Map<String, Map<String, TreeItem<U>>> subcats = new TreeMap<>();
        for (T item : items) {
            String category = getCategory.apply(item);
            categoryStrings.add(category);
            String subCategory = getSubCategory.apply(item);
            TreeItem<U> node = categories.computeIfAbsent(category, (cat) -> new TreeItem<>(makeLabelEntry.apply(cat)));
            if(subCategory == null || subCategory.trim().length() == 0){
                node.getChildren().add(
                        new TreeItem<>(makeEntry.apply(item))
                );
            } else {
                Map<String, TreeItem<U>> subcatMap = subcats.computeIfAbsent(category, (cat) -> new TreeMap<>());
                subcatMap.computeIfAbsent(subCategory, subcat -> new TreeItem<>(
                        makeLabelEntry.apply(subcat))).getChildren().add(
                    new TreeItem<>(makeEntry.apply(item))
                );
            }
        }
        if(categories.size() == 1) {
            Map.Entry<String, TreeItem<U>> cat = categories.entrySet().iterator().next();
            if(subcats.get(cat.getKey()) != null)
                root.getChildren().addAll(subcats.get(cat.getKey()).values());
            root.getChildren().addAll(cat.getValue().getChildren());
        } else if(categories.size() > 1) {
            multiCategory = true;
            for (Map.Entry<String, TreeItem<U>> cat : categories.entrySet()) {
                if(subcats.get(cat.getKey()) != null)
                    cat.getValue().getChildren().addAll(0, subcats.get(cat.getKey()).values());
            }
            root.getChildren().addAll(categories.values());
            for (TreeItem<U> value : categories.values()) {
                value.getChildren().sort(Comparator.comparing(TreeItem::getValue));
            }

        }
    }

    private void insert(T item) {
        if(multiCategory) {
            for (TreeItem<U> cat : getRoot().getChildren()) {
                if(cat.getValue().toString().equals(getCategory.apply(item))) {
                    insertSubcat(cat, item);
                    return;
                }
            }
            TreeItem<U> cat = new TreeItem<>(makeLabelEntry.apply(getCategory.apply(item)));
            categoryStrings.add(getCategory.apply(item));
            getRoot().getChildren().add(cat);
            insertSubcat(cat, item);
            getRoot().getChildren().sort(Comparator.comparing(TreeItem::getValue));
        }else{
            if(categoryStrings.size() == 0 || categoryStrings.contains(getCategory.apply(item)))
                insertSubcat(getRoot(), item);
            else{
                multiCategory = true;
                TreeItem<U> cat = new TreeItem<>(makeLabelEntry.apply(categoryStrings.iterator().next()));
                for (TreeItem<U> child : getRoot().getChildren()) {
                    cat.getChildren().add(deepCopy(child));
                }
                getRoot().getChildren().clear();
                getRoot().getChildren().add(cat);
                insert(item);
            }
        }
    }

    private TreeItem<U> deepCopy(TreeItem<U> original) {
        TreeItem<U> item = new TreeItem<>(original.getValue());
        for (TreeItem<U> child : original.getChildren()) {
            item.getChildren().add(deepCopy(child));
        }
        return item;
    }

    private void insertSubcat(TreeItem<U> root, T item) {
        if(getSubCategory.apply(item) == null || getSubCategory.apply(item).trim().length() == 0) {
            root.getChildren().add(new TreeItem<>(makeEntry.apply(item)));
        }
        else {
            for (TreeItem<U> subcat : root.getChildren()) {
                if(subcat.getValue().toString().equals(getSubCategory.apply(item))) {
                    subcat.getChildren().add(new TreeItem<>(makeEntry.apply(item)));
                    subcat.getChildren().sort(Comparator.comparing(TreeItem::getValue));
                    return;
                }
            }
            TreeItem<U> subcat = new TreeItem<>(makeLabelEntry.apply(getSubCategory.apply(item)));
            root.getChildren().add(subcat);
            categoryStrings.add(getCategory.apply(item));
            subcat.getChildren().add(new TreeItem<>(makeEntry.apply(item)));
        }
        root.getChildren().sort(Comparator.comparing(TreeItem::getValue));
    }

    private void retract(T item) {
        if(multiCategory) {
            for (TreeItem<U> cat : getRoot().getChildren()) {
                if(cat.getValue().toString().equals(getCategory.apply(item))) {
                    retractSubcat(cat, item);
                    if(cat.getChildren().size() == 0) {
                        getRoot().getChildren().remove(cat);
                        categoryStrings.remove(getCategory.apply(item));
                        if(getRoot().getChildren().size() == 1) {
                            multiCategory = false;
                            ObservableList<TreeItem<U>> children = getRoot().getChildren().get(0).getChildren();
                            getRoot().getChildren().clear();
                            for (TreeItem<U> child : children) {
                                getRoot().getChildren().add(deepCopy(child));
                            }
                        }
                    }
                    break;
                }
            }

        }else{
            retractSubcat(getRoot(), item);
        }
    }
    private void retractSubcat(TreeItem<U> root, T item) {
        if(getSubCategory.apply(item) == null || getSubCategory.apply(item).trim().length() == 0) {
            root.getChildren().removeIf(ti->item.equals(ti.getValue().getContents()));
        }
        else {
            for (TreeItem<U> subcat : root.getChildren()) {
                if(subcat.getValue().toString().equals(getSubCategory.apply(item))) {
                    Iterator<TreeItem<U>> iterator = subcat.getChildren().iterator();
                    while(iterator.hasNext()) {
                        TreeItem<U> child = iterator.next();
                        if(item.equals(child.getValue().getContents())) {
                            iterator.remove();
                            break;
                        }
                    }
                    if(subcat.getChildren().size() == 0) {
                        root.getChildren().remove(subcat);
                    }
                    return;
                }
            }

        }
    }

    @Override
    protected void createColumns() {
        this.getColumns().addAll(makeColumns.apply(this.widthProperty()));
    }
}
