package ui.controls.lists;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import ui.controls.lists.entries.ListEntry;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ObservableCategoryEntryList<T, U extends ListEntry<T>> extends ObservableEntryList<T, U> {
    private final Function<T, String> getSubCategory;

    public ObservableCategoryEntryList(ObservableList<T> items,
                                       BiConsumer<T, Integer> handler,
                                       Function<T, String> getCategory,
                                       Function<T, String> getSubCategory,
                                       Function<T, U> makeEntry,
                                        Function<String, U> makeLabelEntry,
                                       Function<ReadOnlyDoubleProperty, List<TreeTableColumn<U, ?>>> makeColumns) {
        super(items, getCategory, makeEntry, makeLabelEntry, makeColumns);
        this.getSubCategory = getSubCategory;
        super.construct(handler);
        getRoot().getChildren().sort(Comparator.comparing(o -> {
            try {
                return Integer.parseInt(o.getValue().toString());
            }catch (NumberFormatException e) {
                return 0;
            }
        }));
    }
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

    @Override
    protected void insertCategoryChild(TreeItem<U> root, T item) {
        if(getSubCategory.apply(item) == null || getSubCategory.apply(item).trim().length() == 0) {
            super.insertCategoryChild(root, item);
            return;
        }
        for (TreeItem<U> subcat : root.getChildren()) {
            if(subcat.getValue().toString().equals(getSubCategory.apply(item))) {
                subcat.getChildren().add(new TreeItem<>(makeEntry.apply(item)));
                subcat.getChildren().sort(Comparator.comparing(TreeItem::getValue));
                return;
            }
        }
        TreeItem<U> subcat = new TreeItem<>(makeLabelEntry.apply(getSubCategory.apply(item)));
        root.getChildren().add(subcat);
        subcat.getChildren().add(new TreeItem<>(makeEntry.apply(item)));
        root.getChildren().sort(Comparator.comparing(TreeItem::getValue));
    }

    @Override
    protected void retractCategoryChild(TreeItem<U> root, T item) {
        if(getSubCategory.apply(item) == null || getSubCategory.apply(item).trim().length() == 0) {
            super.retractCategoryChild(root, item);
            return;
        }
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
