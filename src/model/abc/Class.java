package model.abc;

import model.AbilitySlot;
import model.AttributeMod;
import model.enums.Attribute;
import model.enums.Proficiency;


import static model.enums.Proficiency.*;

import java.util.*;

import static model.enums.Attribute.*;

public class Class {
    private int hp;
    private List<List<AbilitySlot>> advancementTable;
    private String name;

    private static Map<String, Class> classMap = new HashMap<>();

    static {
        List<List<AbilitySlot>> paladinTable = new ArrayList<>();
        List<AbilitySlot> paladin1 = new ArrayList<>();
        List<AttributeMod> mods = new ArrayList<>();
        constructorHelper(mods, Trained, Religion, Perception, Reflex, SimpleWeapons, MartialWeapons, LightArmor, MediumArmor, HeavyArmor, Shields);
        constructorHelper(mods, Expert, Fortitude, Will);
        paladin1.add(new AbilitySlot("Initial Proficiencies", mods));
        paladinTable.add(null);
        paladinTable.add(paladin1);
        classMap.put("Paladin", new Class("Paladin", 10, paladinTable));
    }

    private Class(String name, int hp, List<List<AbilitySlot>> table) {
        this.hp = hp;
        this.name = name;
        advancementTable = table;
    }

    public List<AbilitySlot> getLevel(int level) {
        return Collections.unmodifiableList(advancementTable.get(level));
    }

    public String getName() {
        return name;
    }
    private static void constructorHelper(List<AttributeMod> mods, Proficiency proficiency, Attribute... attributes) {
        for(Attribute attr: attributes) {
            mods.add(new AttributeMod(attr, proficiency));
        }
    }

    public static Class getClass(String selectedItem) {
        return classMap.get(selectedItem);
    }

    public static Set<String> getClassNames() {
        return classMap.keySet();
    }

    public int getHP() {
        return hp;
    }
}
