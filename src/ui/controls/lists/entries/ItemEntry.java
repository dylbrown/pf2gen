package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import model.equipment.Equipment;
import model.equipment.runes.runedItems.RunedEquipment;

import static model.util.StringUtils.generateCostString;

public class ItemEntry implements Comparable<ItemEntry>, TreeTableEntry {
    private final Equipment item;
    private final ReadOnlyStringProperty name;
    private final ReadOnlyStringProperty cost;
    private final ReadOnlyStringProperty level;
    private final ReadOnlyStringProperty subCategory;

    public ItemEntry(Equipment item) {
        this.item = item;
        if(item instanceof RunedEquipment)
            this.name = ((RunedEquipment) item).getRunes().getFullName();
        else
            this.name = new ReadOnlyStringWrapper(item.toString()).getReadOnlyProperty();
        this.cost = new ReadOnlyStringWrapper(generateCostString(item.getValue())).getReadOnlyProperty();
        this.level = new ReadOnlyStringWrapper(String.valueOf(item.getLevel())).getReadOnlyProperty();
        this.subCategory = new ReadOnlyStringWrapper(item.getSubCategory()).getReadOnlyProperty();
    }

    public ItemEntry(String label) {
        this.item = null;
        this.name = new ReadOnlyStringWrapper(label).getReadOnlyProperty();
        this.cost = new ReadOnlyStringWrapper("").getReadOnlyProperty();
        this.level = new ReadOnlyStringWrapper("").getReadOnlyProperty();
        this.subCategory = new ReadOnlyStringWrapper("").getReadOnlyProperty();
    }

    private ReadOnlyStringProperty nameProperty() {
        return name;
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


    @Override
    public int compareTo(ItemEntry o) {
        if(this.getItem() == null && o.getItem() != null) return 1;
        if(this.getItem() != null && o.getItem() == null) return -1;
        return this.toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        if(item == null) return this.name.get();
        else return item.toString();
    }
}

