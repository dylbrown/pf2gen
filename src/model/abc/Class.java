package model.abc;

import model.abilities.Ability;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilityScores.AbilityMod;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Class extends AC {
    private final int skillIncreases;
    private Map<Integer, List<AbilitySlot>> advancementTable;

    public Class(String name, String description, int hp, int skillIncreases, AbilityMod keyAbility, Map<Integer, List<AbilitySlot>> table, List<Ability> feats) {
        super(name, description, Collections.singletonList(keyAbility),hp,feats);
        this.skillIncreases = skillIncreases;
        advancementTable = table;
    }

    public List<AbilitySlot> getLevel(int level) {
        return Collections.unmodifiableList(advancementTable.get(level));
    }

    public int getSkillIncrease() {
        return skillIncreases;
    }
}
