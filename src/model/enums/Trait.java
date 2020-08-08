package model.enums;

import model.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Trait implements Comparable<Trait> {
    private static final Map<String, Trait> traits = new HashMap<>();
    private final String name;
    private Trait(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trait trait = (Trait) o;
        return name.equals(trait.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static Trait valueOf(String name) {
        if(name == null)
            return null;
        name = StringUtils.camelCase(name);
        return traits.computeIfAbsent(name, Trait::new);
    }

    @Override
    public int compareTo(Trait o) {
        return name.compareTo(o.name);
    }
}
