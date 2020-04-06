package ui.controls.equipment.all_items;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import model.data_managers.EquipmentManager;
import model.equipment.Equipment;
import model.xml_parsers.ItemLoader;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class CategoryEquipmentList extends TreeTableView<ItemEntry> {
    public CategoryEquipmentList(Consumer<Equipment> handler) {
        this.setShowRoot(false);
        this.setRowFactory(new SelectRowFactory(handler));
        this.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
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
        this.getColumns().addAll(name, cost, level);
        TreeItem<ItemEntry> root = new TreeItem<>(new ItemEntry("root"));
        for (String category : EquipmentManager.getCategories()) {
            TreeItem<ItemEntry> cat = new TreeItem<>(new ItemEntry(category));
            root.getChildren().add(cat);
            Map<String, TreeItem<ItemEntry>> subCats = new TreeMap<>();
            for (Equipment equipment : EquipmentManager.getItems(category)) {
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
        this.setRoot(root);
        name.minWidthProperty().bind(this.widthProperty().multiply(.6));
    }
}
