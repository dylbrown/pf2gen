package model.equipment;

import javafx.beans.property.*;
import model.enums.Rarity;
import model.enums.Slot;
import model.util.StringUtils;

import java.util.Objects;

public abstract class Equipment implements Comparable<Equipment> {
    private final ReadOnlyDoubleWrapper weight;
    private final ReadOnlyDoubleWrapper value;
    private final ReadOnlyStringWrapper name;
    private final ReadOnlyObjectWrapper<Rarity> rarity;
    private final ReadOnlyStringWrapper description;
    private final ReadOnlyObjectWrapper<Slot> slot;

    Equipment(double weight, double value, String name, String description, Rarity rarity, Slot slot) {
        this.weight = new ReadOnlyDoubleWrapper(weight);
        this.value = new ReadOnlyDoubleWrapper(value);
        this.name = new ReadOnlyStringWrapper(StringUtils.camelCase(name));
        this.description = new ReadOnlyStringWrapper(description);
        this.rarity = new ReadOnlyObjectWrapper<>(rarity);
        this.slot = new ReadOnlyObjectWrapper<>(slot);
    }

    double getWeight() {
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

    String getDescription() {
        return description.get();
    }

    public ReadOnlyStringProperty descriptionProperty() {
        return description.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Slot> slotProperty() {
        return slot.getReadOnlyProperty();
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

    @Override
    public int compareTo(Equipment o) {
        return this.getName().compareTo(o.getName());
    }
}
