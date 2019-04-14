package model;

import model.enums.AbilityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ability {
    private List<AbilityType> attributes = new ArrayList<>();
    private List<AttributeMod> modifiers;
    private String name;
    public Ability(String name) {
        this.name = name;
    }

    public Ability(String name, List<AttributeMod> mods) {
        this(name);
        this.modifiers = mods;
    }

    public List<AttributeMod> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }
}
