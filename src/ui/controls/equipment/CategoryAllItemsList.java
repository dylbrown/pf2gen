package ui.controls.equipment;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import model.equipment.Equipment;
import model.player.PC;
import model.xml_parsers.equipment.EquipmentLoader;
import ui.controls.lists.AbstractEntryList;
import ui.controls.lists.entries.ItemEntry;
import ui.controls.lists.factories.TreeCellFactory;

import java.util.*;
import java.util.function.BiConsumer;

public class CategoryAllItemsList extends AbstractEntryList<Equipment, ItemEntry> {
    private final PC character;

    public CategoryAllItemsList(PC character, BiConsumer<Equipment, Integer> handler) {
        this(character);
        construct(handler);
    }

    protected CategoryAllItemsList(PC character) {
        super();
        this.character = character;
    }

    @Override
    protected void addItems(TreeItem<ItemEntry> root) {
        for (String category : character.sources().equipment().getCategories()) {
            addCategory(root, category, character.sources().equipment().getCategory(category).values());
        }
        addCategory(root, "armor_and_shields", character.sources().armor().getAll().values());
        addCategory(root, "weapons", character.sources().weapons().getAll().values());
        root.getChildren().sort(Comparator.comparing(o -> o.getValue().toString()));
    }

    private void addCategory(TreeItem<ItemEntry> root, String category, Iterable<? extends Equipment> iterable) {
        TreeItem<ItemEntry> cat = new TreeItem<>(new ItemEntry(category));
        root.getChildren().add(cat);
        Map<String, TreeItem<ItemEntry>> subCats = new TreeMap<>();
        for (Equipment equipment : iterable) {
            String subCategory = equipment.getSubCategory();
            if(subCategory.isBlank())
                cat.getChildren().add(new TreeItem<>(new ItemEntry(equipment)));
            else {
                subCats.computeIfAbsent(subCategory, (s -> new TreeItem<>(new ItemEntry(s))))
                        .getChildren()
                        .add(new TreeItem<>(new ItemEntry(equipment)));
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
