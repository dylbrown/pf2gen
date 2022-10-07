package ui.controls.lists;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import model.util.TriConsumer;
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
        this(items, (item, node, count)->handler.accept(item, count),
                getCategory, getSubCategory,
                makeEntry, makeLabelEntry, makeColumns);
    }

    public ObservableCategoryEntryList(ObservableList<T> items,
                                       TriConsumer<T, TreeItem<U>, Integer> handler,
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
                    super.insertCategoryChild(subcat, item);
                    return;
                }
            }
            TreeItem<U> subcat = makeLabelEntry.apply(subCategory);
            categoryRoot.getChildren().add(subcat);
            categoryRoot.getChildren().sort(Comparator.comparing(TreeItem::getValue));
            super.insertCategoryChild(subcat, item);
        }else{
            if(categoryRoot.getChildren().size() > 0) {
                Map<String, Integer> foundDifferent = new HashMap<>();
                int currentFound = 0;
                boolean foundSame = false;
                for (TreeItem<U> child : categoryRoot.getChildren()) {
                    T aChild = child.getValue().getContents();
                    if(aChild != null) {
                        String otherSubCat = getSubCategory.apply(aChild);
                        if(!subCategory.equals(otherSubCat)) {
                            currentFound = foundDifferent.merge(otherSubCat, 1, Integer::sum);
                        }
                        foundSame |= subCategory.equals(otherSubCat);
                    }
                    if(currentFound > 1 || (foundDifferent.size() > 0 && foundSame)) {
                        isMultiSubCategory.put(category, true);
                        expand(categoryRoot, getSubCategory);
                        insertCategoryChild(categoryRoot, item);
                        return;
                    }
                }
            }
            super.insertCategoryChild(categoryRoot, item);
        }
    }

    @Override
    protected void expand(TreeItem<U> root, Function<T, String> grouping) {
        for (TreeItem<U> child : root.getChildren()) {
            if(child.getValue().getContents() == null) {
                TreeItem<U> category = makeLabelEntry.apply(getCategory.apply(
                        child.getChildren().get(0).getValue().getContents()));
                for (TreeItem<U> rootChild : root.getChildren()) {
                    category.getChildren().add(deepCopy(rootChild));
                }
                root.getChildren().setAll(Collections.singletonList(category));
                return;
            }
        }
        super.expand(root, grouping);
    }

    @Override
    protected void retractCategoryChild(TreeItem<U> categoryRoot, T item) {
        String category = getCategory.apply(item);
        String subCategory = getSubCategory.apply(item);
        if(subCategory == null ||
                subCategory.isBlank() ||
                !isMultiSubCategory.getOrDefault(category, false)) {
            super.retractCategoryChild(categoryRoot, item);
            return;
        }
        for (TreeItem<U> subcat : categoryRoot.getChildren()) {
            if(subcat.getValue().toString().equals(subCategory)) {
                subcat.getChildren().removeIf(child->item.equals(child.getValue().getContents()));
                if(subcat.getChildren().size() == 0) {
                    categoryRoot.getChildren().remove(subcat);
                }
                if(checkForChildCollapse(categoryRoot, subcat))
                    isMultiSubCategory.put(category, false);
                if(checkForCollapse(getRoot(), categoryRoot))
                    multiCategory = false;
                return;
            }
        }
    }

    private boolean checkForChildCollapse(TreeItem<U> root, TreeItem<U> child) {
        if(checkForCollapse(root, child))
            return true;
        for (TreeItem<U> rootChild : root.getChildren()) {
            if(rootChild.getChildren().size() > 1)
                return false;
        }
        List<TreeItem<U>> newChildren = new ArrayList<>();
        for (TreeItem<U> rootChild : root.getChildren()) {
            newChildren.add(deepCopy(rootChild.getChildren().get(0)));
        }
        root.getChildren().setAll(newChildren);
        return true;
    }
}
