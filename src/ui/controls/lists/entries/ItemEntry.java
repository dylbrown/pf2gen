package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import model.equipment.Equipment;
import model.equipment.runes.runedItems.RunedEquipment;

import static model.util.StringUtils.generateCostString;

public class ItemEntry extends ListEntry<Equipment> {
    private final ReadOnlyStringProperty cost;
    private final ReadOnlyStringProperty level;
    private final ReadOnlyStringProperty subCategory;


    public ItemEntry(Equipment item) {
        super(item, getName(item));
        this.cost = new ReadOnlyStringWrapper(generateCostString(item.getValue())).getReadOnlyProperty();
        this.level = new ReadOnlyStringWrapper(String.valueOf(item.getLevel())).getReadOnlyProperty();
        this.subCategory = new ReadOnlyStringWrapper(item.getSubCategory()).getReadOnlyProperty();
    }

    private static ReadOnlyStringProperty getName(Equipment item) {
        if(item instanceof RunedEquipment)
            //noinspection rawtypes
            return ((RunedEquipment) item).getRunes().getFullName();
        else
            return new ReadOnlyStringWrapper(item.toString()).getReadOnlyProperty();
    }

    public ItemEntry(String label) {
        super(label);
        this.cost = new ReadOnlyStringWrapper("").getReadOnlyProperty();
        this.level = new ReadOnlyStringWrapper("").getReadOnlyProperty();
        this.subCategory = new ReadOnlyStringWrapper("").getReadOnlyProperty();
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

    private ReadOnlyStringProperty costProperty() {
        return cost;
    }

    private ReadOnlyStringProperty levelProperty() {
        return level;
    }

    private ReadOnlyStringProperty subCategoryProperty() {
        return subCategory;
    }
}
