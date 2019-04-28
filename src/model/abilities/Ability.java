package model.abilities;

import model.AttributeMod;
import model.enums.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ability {
    private List<Type> attributes;
    protected List<AttributeMod> modifiers;
    private String name;
    private String description;
    public Ability(String name) {
        this.name = name;
    }

    public Ability(String name, List<AttributeMod> mods) {
        this(name);
        this.modifiers = mods;
    }

    public Ability(String name, String description) {
        this(name);
        this.description = description;
        modifiers=new ArrayList<>();
    }

    public Ability(String name, List<AttributeMod> mods, String description) {
        this(name, mods);
        this.description = description;
    }

    public List<AttributeMod> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    public String getDesc() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }
}
