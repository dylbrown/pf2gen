package ui.controls.lists;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import model.data_managers.sources.SourcesLoader;
import model.equipment.Equipment;
import model.xml_parsers.equipment.EquipmentLoader;
import ui.controls.lists.entries.ItemEntry;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class LevelAllItemsList extends AbstractItemList {
    public LevelAllItemsList(BiConsumer<Equipment, Integer> handler) {
        super(handler);
    }

    @Override
    void createColumns() {
        TreeTableColumn<ItemEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<ItemEntry, String> cost = new TreeTableColumn<>("Cost");
        TreeTableColumn<ItemEntry, String> subCat = new TreeTableColumn<>("Subcategory");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        cost.setCellValueFactory(new TreeCellFactory<>("cost"));
        cost.setStyle( "-fx-alignment: CENTER;");
        subCat.setCellValueFactory(new TreeCellFactory<>("subCategory"));
        subCat.setStyle( "-fx-alignment: CENTER;");
        cost.setComparator(Comparator.comparingDouble(EquipmentLoader::getPrice));
        //noinspection unchecked
        this.getColumns().addAll(name, cost, subCat);
        name.minWidthProperty().bind(this.widthProperty().multiply(.5));
    }

    @Override
    void addItems(TreeItem<ItemEntry> root) {
        Map<Integer, Map<String, TreeItem<ItemEntry>>> cats = new TreeMap<>();
        for (Equipment equipment : SourcesLoader.instance().find("Core Rulebook")
                .getEquipment().getAll().values()) {
            int level = equipment.getLevel();
            cats.computeIfAbsent(level, (s)->new HashMap<>())
                    .computeIfAbsent(equipment.getCategory(), (s)->new TreeItem<>(new ItemEntry(s)))
                    .getChildren().add(new TreeItem<>(new ItemEntry(equipment)));
        }
        for (Map.Entry<Integer, Map<String, TreeItem<ItemEntry>>> entry : cats.entrySet()) {
            TreeItem<ItemEntry> level = new TreeItem<>(new ItemEntry(String.valueOf(entry.getKey())));
            root.getChildren().add(level);
            level.getChildren().addAll(entry.getValue().values());
        }
    }


}
