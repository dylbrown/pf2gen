package ui.controls.equipment;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import model.data_managers.EquipmentManager;
import model.equipment.Equipment;
import model.xml_parsers.ItemLoader;

import java.util.Comparator;
import java.util.function.Consumer;

public abstract class EquipmentList extends TreeTableView<ItemEntry> {
    public static void init(TreeTableView<ItemEntry> allItems, Consumer<Equipment> handler) {
        allItems.setShowRoot(false);
        allItems.setRowFactory(new SelectRowFactory(handler));
        allItems.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        TreeTableColumn<ItemEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<ItemEntry, String> cost = new TreeTableColumn<>("Cost");
        TreeTableColumn<ItemEntry, String> level = new TreeTableColumn<>("Level");
        name.setCellValueFactory(new TreeCellFactory("name"));
        cost.setCellValueFactory(new TreeCellFactory("cost"));
        cost.setStyle( "-fx-alignment: CENTER;");
        level.setCellValueFactory(new TreeCellFactory("level"));
        level.setStyle( "-fx-alignment: CENTER;");
        cost.setComparator(Comparator.comparingDouble(ItemLoader::getPrice));
        level.setComparator((s1,s2)->{
            double d1 = (!s1.equals(""))? Double.valueOf(s1) : 0;
            double d2 = (!s2.equals(""))? Double.valueOf(s2) : 0;
            return Double.compare(d1, d2);
        });
        //noinspection unchecked
        allItems.getColumns().addAll(name, cost, level);
        TreeItem<ItemEntry> root = new TreeItem<>(new ItemEntry("root"));
        for (String category : EquipmentManager.getCategories()) {
            TreeItem<ItemEntry> cat = new TreeItem<>(new ItemEntry(category));
            root.getChildren().add(cat);
            for (Equipment equipment : EquipmentManager.getItems(category)) {
                cat.getChildren().add(new TreeItem<>(new ItemEntry(equipment)));
            }

        }
        allItems.setRoot(root);
        name.minWidthProperty().bind(allItems.widthProperty().multiply(.6));
    }
}
