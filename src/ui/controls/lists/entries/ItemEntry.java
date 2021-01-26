package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import model.enums.Trait;
import model.items.Item;
import model.items.runes.runedItems.Runes;
import model.util.TransformationProperty;

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

    private static ObservableValue<String> getName(Item item) {
        StringBuilder suffix = new StringBuilder();
        for (Trait trait : item.getTraits()) {
            switch (trait.getName().toLowerCase()) {
                case "uncommon":
                    suffix.append("ᵁ");
                    break;
                case "rare":
                    suffix.append("ᴿ");
                    break;
                case "unique":
                    suffix.append("*");
                    break;
            }
        }
        Runes<?> runes = Runes.getRunes(item);
        ReadOnlyObjectProperty<String> name;
        if(runes != null)
            name = runes.getFullName();
        else
            name = new ReadOnlyObjectWrapper<>(item.getName()).getReadOnlyProperty();
        String suffixString = suffix.toString();
        return new TransformationProperty<>(name, s -> s + suffixString);
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

    @Override
    public int compareTo(ListEntry<Item> o) {
        if(this.getContents() == null && o.getContents() == null) {
            String s1 = this.toString();
            String s2 = o.toString();
            if(s1.matches("\\ALevel \\d{1,2}\\z") && s2.matches("\\ALevel \\d{1,2}\\z")) {
                int i1 = Integer.parseInt(s1.substring("Level ".length()));
                int i2 = Integer.parseInt(s2.substring("Level ".length()));
                return Integer.compare(i1, i2);
            }
        }
        return super.compareTo(o);
    }
}
