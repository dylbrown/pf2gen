package model.equipment;

import model.abilities.Ability;
import model.attributes.AttributeBonus;
import model.enums.Rarity;
import model.enums.Slot;
import model.enums.Trait;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Equipment implements Comparable<Equipment> {
    private final double weight, value;
    private final String name, category, subCategory;
    private final int page;
    private final Rarity rarity;
    private final String description;
    private final Slot slot;
    private final List<Trait> traits;
    private final int hands, level;
    private final List<AttributeBonus> bonuses;
    private final List<Ability> abilities;

    protected Equipment(Equipment.Builder builder) {
        this.weight = builder.weight;
        this.value = builder.value;
        this.name = builder.name;
        this.category = builder.category;
        this.subCategory = builder.subCategory;
        this.page = builder.page;
        this.description = builder.description;
        this.rarity = builder.rarity;
        this.slot = builder.slot;
        this.traits = (builder.traits.size() > 0) ? builder.traits : Collections.emptyList();
        this.hands = builder.hands;
        this.level = builder.level;
        this.bonuses = builder.bonuses;
        this.abilities = builder.abilities;
    }

    public double getWeight() {
        return weight;
    }

    public double getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public int getPage() {return page;}

    public Rarity getRarity() {
        return rarity;
    }

    public String getPrettyWeight() {
        if(weight == 0) return "";
        if(weight >= 1)
            return String.valueOf(Math.round(weight));
        else
            return ((weight != .1) ? Math.floor(weight)*10 : "") +"L";
    }

    public String getDesc() {
        return description;
    }

    public Slot getSlot() {
        return slot;
    }

    public int getHands() {
        return hands;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getCategory() {return category;}

    public String getSubCategory() {return subCategory;}

    public Equipment copy() {
        Builder builder = new Builder(this);
        return builder.build();
    }

    public List<Trait> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    public List<AttributeBonus> getBonuses() {
        return Collections.unmodifiableList(bonuses);
    }

    public List<Ability> getAbilities() {
        return Collections.unmodifiableList(abilities);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return Double.compare(equipment.weight, weight) == 0 &&
                Double.compare(equipment.value, value) == 0 &&
                page == equipment.page &&
                hands == equipment.hands &&
                level == equipment.level &&
                name.equals(equipment.name) &&
                Objects.equals(category, equipment.category) &&
                Objects.equals(subCategory, equipment.subCategory) &&
                rarity == equipment.rarity &&
                Objects.equals(description, equipment.description) &&
                slot == equipment.slot &&
                Objects.equals(traits, equipment.traits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weight, value, name, category, subCategory, page, rarity, description, slot, traits, hands, level);
    }

    @Override
    public int compareTo(Equipment o) {
        return this.getName().compareTo(o.getName());
    }

    public static class Builder {
        double weight = 0;
        double value = 0;
        String name = "";
        String category = "";
        String subCategory = "";
        int page = -1;
        Rarity rarity = Rarity.Common;
        String description = "";
        Slot slot = Slot.None;
        List<Trait> traits = new ArrayList<>();
        private int hands;
        private int level;
        private List<AttributeBonus> bonuses = Collections.emptyList();
        private List<Ability> abilities = Collections.emptyList();

        public Builder() {}

        public Builder(Equipment equipment) {
            this.weight = equipment.getWeight();
            this.value = equipment.getValue();
            this.name = equipment.getName();
            this.page = equipment.getPage();
            this.rarity = equipment.getRarity();
            this.description = equipment.getDesc();
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

        public void setCategory(String category) {
            this.category = category;
        }

        public void setSubCategory(String subCategory) {
            this.subCategory = subCategory;
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

        public void setHands(int hands) {
            this.hands = hands;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void addBonus(AttributeBonus bonus) {
            if(bonuses.size() == 0) bonuses = new ArrayList<>();
            bonuses.add(bonus);
        }

        public void addAbility(Ability ability) {
            if(abilities.size() == 0) abilities = new ArrayList<>();
            abilities.add(ability);
        }

        public Equipment build() {
            return new Equipment(this);
        }
    }
}
