package model.equipment;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import model.enums.Rarity;
import model.enums.Slot;

public class SearchItem extends Equipment {

    public SearchItem(String name) {
        super(new Equipment.Builder().setName(name));
    }

    @Override
    public Equipment copy() {
        return new SearchItem(getName());
    }

    @Override
    double getWeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rarity getRarity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrettyWeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDesc() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Slot getSlot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReadOnlyDoubleProperty weightProperty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReadOnlyDoubleProperty valueProperty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReadOnlyObjectProperty<Rarity> rarityProperty() {
        throw new UnsupportedOperationException();
    }

    @Override
    String getDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReadOnlyStringProperty descriptionProperty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReadOnlyObjectProperty<Slot> slotProperty() {
        throw new UnsupportedOperationException();
    }
}
