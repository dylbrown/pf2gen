package ui.controls.equipment.all_items;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import model.equipment.Equipment;

import static model.util.StringUtils.generateCostString;

public class ItemEntry {
    private final Equipment item;
    private final ReadOnlyObjectWrapper<String> name;
    private final ReadOnlyObjectWrapper<String> cost;
    private final ReadOnlyObjectWrapper<String> level;
    private final ReadOnlyObjectWrapper<String> subCategory;

    ItemEntry(Equipment item) {
        this.item = item;
        this.name = new ReadOnlyObjectWrapper<>(item.toString());
        this.cost = new ReadOnlyObjectWrapper<>(generateCostString(item.getValue()));
        this.level = new ReadOnlyObjectWrapper<>(String.valueOf(item.getLevel()));
        this.subCategory = new ReadOnlyObjectWrapper<>(item.getSubCategory());
    }

    ItemEntry(String label) {
        this.item = null;
        this.name = new ReadOnlyObjectWrapper<>(label);
        this.cost = new ReadOnlyObjectWrapper<>("");
        this.level = new ReadOnlyObjectWrapper<>("");
        this.subCategory = new ReadOnlyObjectWrapper<>("");
    }

    private ReadOnlyObjectProperty<String> nameProperty() {
        return name.getReadOnlyProperty();
    }

    private ReadOnlyObjectProperty<String> costProperty() {
        return cost.getReadOnlyProperty();
    }

    private ReadOnlyObjectProperty<String> levelProperty() {
        return level.getReadOnlyProperty();
    }

    private ReadOnlyObjectProperty<String> subCategoryProperty() {
        return subCategory.getReadOnlyProperty();
    }

    public Equipment getItem() {
        return item;
    }


    public ObservableValue<String> get(String propertyName) {
        switch (propertyName){
            case "name": return nameProperty();
            case "cost": return costProperty();
            case "level": return levelProperty();
            case "subCategory": return subCategoryProperty();
        }
        return null;
    }
}

