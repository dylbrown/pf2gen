package model.equipment;

import javafx.beans.property.*;
import model.enums.Rarity;
import model.enums.Slot;

import java.util.Objects;

public abstract class Equipment {
    private ReadOnlyDoubleWrapper weight;
    private ReadOnlyDoubleWrapper value;
    private ReadOnlyStringWrapper name;
    private ReadOnlyObjectWrapper<Rarity> rarity;
    private ReadOnlyStringWrapper description;
    private ReadOnlyObjectWrapper<Slot> slot;
    private ReadOnlyIntegerWrapper count;

    public Equipment(double weight, double value, String name, String description, Rarity rarity, Slot slot) {
        this.weight = new ReadOnlyDoubleWrapper(weight);
        this.value = new ReadOnlyDoubleWrapper(value);
        this.name = new ReadOnlyStringWrapper(name);
        this.description = new ReadOnlyStringWrapper(description);
        this.rarity = new ReadOnlyObjectWrapper<>(rarity);
        this.slot = new ReadOnlyObjectWrapper<>(slot);
        this.count = new ReadOnlyIntegerWrapper(1);
    }

    public double getWeight() {
        return weight.get();
    }

    public double getValue() {
        return value.get();
    }

    public String getName() {
        return name.get();
    }

    public Rarity getRarity() {
        return rarity.get();
    }

    public String getPrettyWeight() {
        if(weight.get() >= 1)
            return String.valueOf(Math.round(weight.get()));
        else
            return ((weight.get() != .1) ? Math.floor(weight.get())*10 : "") +"L";
    }

    public String getDesc() {
        return description.get();
    }

    public Slot getSlot() {
        return slot.get();
    }

    @Override
    public String toString() {
        return name.get();
    }

    public ReadOnlyDoubleProperty weightProperty() {
        return weight.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty valueProperty() {
        return value.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Rarity> rarityProperty() {
        return rarity.getReadOnlyProperty();
    }

    public String getDescription() {
        return description.get();
    }

    public int getCount() {
        return count.get();
    }

    public ReadOnlyStringProperty descriptionProperty() {
        return description.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Slot> slotProperty() {
        return slot.getReadOnlyProperty();
    }
    public ReadOnlyIntegerProperty countProperty() {
        return count.getReadOnlyProperty();
    }

    public void add(int e) {
        count.set(count.get()+e);
    }

    public void remove(int e) {
        count.set(count.get()-e);
    }

    public void setCount(int count) {
        this.count.set(count);
    }

    public abstract Equipment copy();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return weight.equals(equipment.weight) &&
                value.equals(equipment.value) &&
                name.equals(equipment.name) &&
                rarity.equals(equipment.rarity) &&
                Objects.equals(description, equipment.description) &&
                slot.equals(equipment.slot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weight, value, name, rarity, description, slot);
    }
}
