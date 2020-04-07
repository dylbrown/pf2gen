package model.equipment;

import java.util.Objects;

public class CustomTrait {
    private final String name;
    private final String effect;

    public CustomTrait(String name, String effect) {
        this.name = name;
        this.effect = effect;
    }

    public CustomTrait(String name) {
        this.name = name;
        effect = "";
    }

    public String getName() {
        return name;
    }

    String getEffect() {
        return effect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomTrait customTrait = (CustomTrait) o;
        return name.toLowerCase().equals(customTrait.name.toLowerCase());
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
