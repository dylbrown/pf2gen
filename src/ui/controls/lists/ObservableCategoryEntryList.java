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
    private final Map<String, Boolean> isMultiSubCategory = new HashMap<>();

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
    protected void insertCategoryChild(TreeItem<U> categoryRoot, T item) {
        String category = getCategory.apply(item);
        String subCategory = getSubCategory.apply(item);
        if(subCategory == null || subCategory.isBlank()) {
            super.insertCategoryChild(categoryRoot, item);
            return;
        }
        if(isMultiSubCategory.getOrDefault(category, false)) {
            for (TreeItem<U> subcat : categoryRoot.getChildren()) {
                if (subcat.getValue().toString().equals(subCategory)) {
                    subcat.getChildren().add(new TreeItem<>(makeEntry.apply(item)));
                    subcat.getChildren().sort(Comparator.comparing(TreeItem::getValue));
                    return;
                }
            }
            TreeItem<U> subcat = new TreeItem<>(makeLabelEntry.apply(subCategory));
            categoryRoot.getChildren().add(subcat);
            subcat.getChildren().add(new TreeItem<>(makeEntry.apply(item)));
        }else{
            if(categoryRoot.getChildren().size() > 0) {
                T aChild = categoryRoot.getChildren().get(0).getValue().getContents();
                if(!getSubCategory.apply(aChild).equals(subCategory)) {
                    isMultiSubCategory.put(category, true);
                    expand(categoryRoot, getSubCategory.apply(aChild));
                    insertCategoryChild(categoryRoot, item);
                    return;
                }
            }
            categoryRoot.getChildren().add(new TreeItem<>(makeEntry.apply(item)));
        }
        categoryRoot.getChildren().sort(Comparator.comparing(TreeItem::getValue));
    }

    @Override
    protected void retractCategoryChild(TreeItem<U> categoryRoot, T item) {
        if(getSubCategory.apply(item) == null ||
                getSubCategory.apply(item).isBlank() ||
                !isMultiSubCategory.getOrDefault(getCategory.apply(item), false)) {
            super.retractCategoryChild(categoryRoot, item);
            return;
        }
        for (TreeItem<U> subcat : categoryRoot.getChildren()) {
            if(subcat.getValue().toString().equals(getSubCategory.apply(item))) {
                subcat.getChildren().removeIf(child->item.equals(child.getValue().getContents()));
                if(checkForCollapse(categoryRoot))
                    isMultiSubCategory.put(getCategory.apply(item), false);
                if(checkForCollapse(getRoot()))
                    multiCategory = false;
                return;
            }
        }
    }
}
