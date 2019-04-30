package model.abilities;

import model.AttributeMod;
import model.enums.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ability {
    private List<String> prerequisites;
    private List<Type> attributes;
    protected List<AttributeMod> modifiers;
    private String name;
    private String description;
    private int level;

    private Ability(String name, int level,  String description, List<String> prerequisites) {
        this.name = name;
        this.description = description;
        this.prerequisites = prerequisites;
        this.level = level;
    }

    public Ability(int level, String name, String description, List<String> prerequisites) {
        this(name, level, description, prerequisites);
        this.modifiers=new ArrayList<>();
    }

    public Ability(int level, String name, List<AttributeMod> mods, String description, List<String> prerequisites) {
        this(name, level, description, prerequisites);
        this.modifiers = mods;
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

    public int getLevel() {
        return level;
    }
}
