package ui.controls.equipment.lists;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import model.equipment.Equipment;

import java.util.*;
import java.util.function.BiConsumer;

public class ItemsList extends CategoryAllItemsList {
    private final ObservableList<Equipment> items;

    public ItemsList(ObservableList<Equipment> items, BiConsumer<Equipment, Integer> handler) {
        super();
        this.items = items;
        this.items.addListener((ListChangeListener<Equipment>) c -> {
            while(c.next()) {
                if(!c.wasPermutated() && !c.wasUpdated()) {
                    for (Equipment item : c.getRemoved()) {
                        retract(item);
                    }
                    for (Equipment item : c.getAddedSubList()) {
                        insert(item);
                    }
                }
            }
        });
        super.construct(handler);
    }

    private boolean multiCategory = false;
    private Set<String> categoryStrings = new HashSet<>();
    @Override
    void addItems(TreeItem<ItemEntry> root) {
        Map<String, TreeItem<ItemEntry>> categories = new TreeMap<>();
        Map<String, Map<String, TreeItem<ItemEntry>>> subcats = new TreeMap<>();
        for (Equipment item : items) {
            String category = item.getCategory();
            categoryStrings.add(category);
            String subCategory = item.getSubCategory();
            TreeItem<ItemEntry> node = categories.computeIfAbsent(category, (cat) -> new TreeItem<>(new ItemEntry(cat)));
            if(subCategory == null || subCategory.trim().length() == 0) node.getChildren().add(
                    new TreeItem<>(new ItemEntry(item))
            );
            else {
                Map<String, TreeItem<ItemEntry>> subcatMap = subcats.computeIfAbsent(category, (cat) -> new TreeMap<>());
                subcatMap.computeIfAbsent(subCategory, subcat -> new TreeItem<>(new ItemEntry(subCategory))).getChildren().add(
                    new TreeItem<>(new ItemEntry(item))
                );
            }
        }
        if(categories.size() == 1) {
            Map.Entry<String, TreeItem<ItemEntry>> cat = categories.entrySet().iterator().next();
            if(subcats.get(cat.getKey()) != null)
                root.getChildren().addAll(subcats.get(cat.getKey()).values());
            root.getChildren().addAll(cat.getValue().getChildren());
        } else if(categories.size() > 1) {
            multiCategory = true;
            for (Map.Entry<String, TreeItem<ItemEntry>> cat : categories.entrySet()) {
                if(subcats.get(cat.getKey()) != null)
                    cat.getValue().getChildren().addAll(0, subcats.get(cat.getKey()).values());
            }
            root.getChildren().addAll(categories.values());
        }
    }

    private void insert(Equipment item) {
        if(multiCategory) {
            for (TreeItem<ItemEntry> cat : getRoot().getChildren()) {
                if(cat.getValue().toString().equals(item.getCategory())) {
                    insertSubcat(cat, item);
                    return;
                }
            }
            TreeItem<ItemEntry> cat = new TreeItem<>(new ItemEntry(item.getCategory()));
            categoryStrings.add(item.getCategory());
            getRoot().getChildren().add(cat);
            insertSubcat(cat, item);
            getRoot().getChildren().sort(Comparator.comparing(TreeItem::getValue));
        }else{
            if(categoryStrings.size() == 0 || categoryStrings.contains(item.getCategory()))
                insertSubcat(getRoot(), item);
            else{
                multiCategory = true;
                TreeItem<ItemEntry> cat = new TreeItem<>(new ItemEntry(categoryStrings.iterator().next()));
                for (TreeItem<ItemEntry> child : getRoot().getChildren()) {
                    cat.getChildren().add(deepCopy(child));
                }
                getRoot().getChildren().clear();
                getRoot().getChildren().add(cat);
                insert(item);
            }
        }
    }

    private TreeItem<ItemEntry> deepCopy(TreeItem<ItemEntry> original) {
        TreeItem<ItemEntry> item = new TreeItem<>(original.getValue());
        for (TreeItem<ItemEntry> child : original.getChildren()) {
            item.getChildren().add(deepCopy(child));
        }
        return item;
    }

    private void insertSubcat(TreeItem<ItemEntry> root, Equipment item) {
        if(item.getSubCategory() == null || item.getSubCategory().trim().length() == 0) {
            root.getChildren().add(new TreeItem<>(new ItemEntry(item)));
            root.getChildren().sort(Comparator.comparing(TreeItem::getValue));
        }
        else {
            for (TreeItem<ItemEntry> subcat : root.getChildren()) {
                if(subcat.getValue().toString().equals(item.getSubCategory())) {
                    subcat.getChildren().add(new TreeItem<>(new ItemEntry(item)));
                    subcat.getChildren().sort(Comparator.comparing(TreeItem::getValue));
                    return;
                }
            }
            TreeItem<ItemEntry> subcat = new TreeItem<>(new ItemEntry(item.getSubCategory()));
            root.getChildren().add(subcat);
            categoryStrings.add(item.getCategory());
            subcat.getChildren().add(new TreeItem<>(new ItemEntry(item)));
            root.getChildren().sort(Comparator.comparing(TreeItem::getValue));
        }
    }

    private void retract(Equipment item) {
        if(multiCategory) {
            for (TreeItem<ItemEntry> cat : getRoot().getChildren()) {
                if(cat.getValue().toString().equals(item.getCategory())) {
                    retractSubcat(cat, item);
                    if(cat.getChildren().size() == 0) {
                        getRoot().getChildren().remove(cat);
                        categoryStrings.remove(item.getCategory());
                        if(getRoot().getChildren().size() == 1) {
                            multiCategory = false;
                            ObservableList<TreeItem<ItemEntry>> children = getRoot().getChildren().get(0).getChildren();
                            getRoot().getChildren().clear();
                            for (TreeItem<ItemEntry> child : children) {
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
    private void retractSubcat(TreeItem<ItemEntry> root, Equipment item) {
        if(item.getSubCategory() == null || item.getSubCategory().trim().length() == 0) {
            root.getChildren().removeIf(ti->item.equals(ti.getValue().getItem()));
        }
        else {
            for (TreeItem<ItemEntry> subcat : root.getChildren()) {
                if(subcat.getValue().toString().equals(item.getSubCategory())) {
                    Iterator<TreeItem<ItemEntry>> iterator = subcat.getChildren().iterator();
                    while(iterator.hasNext()) {
                        TreeItem<ItemEntry> child = iterator.next();
                        if(item.equals(child.getValue().getItem())) {
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
}
