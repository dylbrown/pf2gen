package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import model.items.Item;
import model.items.runes.runedItems.Runes;

import static model.util.StringUtils.generateCostString;

public class ItemEntry extends ListEntry<Item> {
    private final ReadOnlyStringProperty cost;
    private final ReadOnlyStringProperty level;
    private final ReadOnlyStringProperty subCategory;


    public ItemEntry(Item item) {
        super(item, getName(item));
        this.cost = new ReadOnlyStringWrapper(generateCostString(item.getValue())).getReadOnlyProperty();
        this.level = new ReadOnlyStringWrapper(String.valueOf(item.getLevel())).getReadOnlyProperty();
        this.subCategory = new ReadOnlyStringWrapper(item.getSubCategory()).getReadOnlyProperty();
    }

    private static ReadOnlyStringProperty getName(Item item) {
        Runes<?> runes = Runes.getRunes(item);
        if(runes != null)
            return runes.getFullName();
        else
            return new ReadOnlyStringWrapper(item.getName()).getReadOnlyProperty();
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
