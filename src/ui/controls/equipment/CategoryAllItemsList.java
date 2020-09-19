package ui.controls.equipment;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import model.equipment.Item;
import model.player.PC;
import model.xml_parsers.equipment.EquipmentLoader;
import ui.controls.lists.ObservableCategoryEntryList;
import ui.controls.lists.entries.ItemEntry;
import ui.controls.lists.factories.TreeCellFactory;

import java.util.*;
import java.util.function.BiConsumer;

public class CategoryAllItemsList extends ObservableCategoryEntryList<Item, ItemEntry> {
    private final PC character;
    private final ObservableList<Item> list;

    public CategoryAllItemsList(PC character, BiConsumer<Item, Integer> handler) {
        this(character, handler, FXCollections.observableArrayList());
    }

    private CategoryAllItemsList(PC character, BiConsumer<Item, Integer> handler, ObservableList<Item> items) {
        super(items, handler,
                Item::getCategory,
                Item::getSubCategory,
                ItemEntry::new, ItemEntry::new,
                CategoryAllItemsList::makeColumns);
        list = items;
        this.character = character;
        addItems(getRoot());
    }

    @Override
    protected void addItems(TreeItem<ItemEntry> root) {
        if(list == null)
            return;
        list.addAll(character.sources().equipment().getAll().values());
        list.addAll(character.sources().armor().getAll().values());
        list.addAll(character.sources().weapons().getAll().values());
        root.getChildren().sort(Comparator.comparing(o -> o.getValue().toString()));
    }

    private void addCategory(TreeItem<ItemEntry> root, String category, Iterable<? extends Item> iterable) {
        TreeItem<ItemEntry> cat = new TreeItem<>(new ItemEntry(category));
        root.getChildren().add(cat);
        Map<String, TreeItem<ItemEntry>> subCats = new TreeMap<>();
        for (Item item : iterable) {
            String subCategory = item.getSubCategory();
            if(subCategory.isBlank())
                cat.getChildren().add(new TreeItem<>(new ItemEntry(item)));
            else {
                subCats.computeIfAbsent(subCategory, (s -> new TreeItem<>(new ItemEntry(s))))
                        .getChildren()
                        .add(new TreeItem<>(new ItemEntry(item)));
            }
        }
        cat.getChildren().addAll(subCats.values());
    }

    public static List<TreeTableColumn<ItemEntry, ?>> makeColumns(ReadOnlyDoubleProperty width) {
        TreeTableColumn<ItemEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<ItemEntry, String> cost = new TreeTableColumn<>("Cost");
        TreeTableColumn<ItemEntry, String> level = new TreeTableColumn<>("Level");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        name.minWidthProperty().bind(width.multiply(.6));
        cost.setCellValueFactory(new TreeCellFactory<>("cost"));
        cost.setStyle( "-fx-alignment: CENTER;");
        level.setCellValueFactory(new TreeCellFactory<>("level"));
        level.setStyle( "-fx-alignment: CENTER;");
        cost.setComparator(Comparator.comparingDouble(EquipmentLoader::getPrice));
        level.setComparator((s1,s2)->{
            double d1 = (!s1.equals(""))? Double.parseDouble(s1) : 0;
            double d2 = (!s2.equals(""))? Double.parseDouble(s2) : 0;
            return Double.compare(d1, d2);
        });
        return Arrays.asList(name, cost, level);
    }

    @Override
    protected void createColumns() {
        this.getColumns().addAll(makeColumns(this.widthProperty()));
    }
}
