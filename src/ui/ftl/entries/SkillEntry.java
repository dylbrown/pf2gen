package ui.ftl.entries;

import model.ability_scores.AbilityScore;
import model.enums.Attribute;

import java.util.function.Supplier;

public class SkillEntry {
    private final Attribute name;
    private final Supplier<Integer> mod;

    public SkillEntry(Attribute name, Supplier<Integer> mod) {
        this.name = name;
        this.mod = mod;
    }

    public Attribute getName() { return name; }

    public AbilityScore getAbility() {
        return name.getKeyAbility();
    }

    public int getMod() {return mod.get();}
}
