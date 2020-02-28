package model.equipment;

import javafx.beans.property.*;
import model.enums.Rarity;
import model.enums.Slot;
import model.enums.Trait;
import model.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Equipment implements Comparable<Equipment> {
    private final ReadOnlyDoubleWrapper weight;
    private final ReadOnlyDoubleWrapper value;
    private final ReadOnlyStringWrapper name;
    private final ReadOnlyIntegerWrapper page;
    private final ReadOnlyObjectWrapper<Rarity> rarity;
    private final ReadOnlyStringWrapper description;
    private final ReadOnlyObjectWrapper<Slot> slot;
    private final ReadOnlyObjectWrapper<List<Trait>> traits;

    Equipment(Equipment.Builder builder) {
        this.weight = new ReadOnlyDoubleWrapper(builder.weight);
        this.value = new ReadOnlyDoubleWrapper(builder.value);
        this.name = new ReadOnlyStringWrapper(StringUtils.camelCase(builder.name));
        this.page = new ReadOnlyIntegerWrapper(builder.page);
        this.description = new ReadOnlyStringWrapper(builder.description);
        this.rarity = new ReadOnlyObjectWrapper<>(builder.rarity);
        this.slot = new ReadOnlyObjectWrapper<>(builder.slot);
        this.traits = new ReadOnlyObjectWrapper<>((builder.traits.size() > 0) ? builder.traits : Collections.emptyList());
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

    public int getPage() {return page.get();}

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

    public Equipment copy() {
        Builder builder = new Builder(this);
        return builder.build();
    }

    private List<Trait> getTraits() {
        return Collections.unmodifiableList(traits.get());
    }

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

    public static class Builder {
        double weight = 0;
        double value = 0;
        String name = "";
        int page = -1;
        Rarity rarity = Rarity.Common;
        String description = "";
        Slot slot = Slot.None;
        List<Trait> traits = new ArrayList<>();

        public Builder() {}

        public Builder(Equipment equipment) {
            this.weight = equipment.getWeight();
            this.value = equipment.getValue();
            this.name = equipment.getName();
            this.page = equipment.getPage();
            this.rarity = equipment.getRarity();
            this.description = equipment.getDescription();
            this.slot = equipment.getSlot();
            this.traits = new ArrayList<>(equipment.getTraits());
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public void setRarity(Rarity rarity) {
            this.rarity = rarity;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setSlot(Slot slot) {
            this.slot = slot;
        }

        public void addTrait(Trait trait) {
            traits.add(trait);
        }

        public Equipment build() {
            return new Equipment(this);
        }
    }
}
