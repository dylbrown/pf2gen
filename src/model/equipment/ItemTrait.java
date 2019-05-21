package model.equipment;

import java.util.Objects;

public class ItemTrait {
    private String name;
    private String effect;

    public ItemTrait(String name, String effect) {
        this.name = name;
        this.effect = effect;
    }

    public ItemTrait(String name) {
        this.name = name;
        effect = "";
    }

    public String getName() {
        return name;
    }

    public String getEffect() {
        return effect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemTrait itemTrait = (ItemTrait) o;
        return name.toLowerCase().equals(itemTrait.name.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
