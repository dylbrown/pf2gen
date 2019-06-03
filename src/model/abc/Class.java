package model.abc;

import model.abilities.Ability;
import model.abilities.abilitySlots.AbilitySlot;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityScore;
import model.enums.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Class extends AC {
    public static final Class NO_CLASS = new Class("No Class", "", 0, 0, new AbilityMod(AbilityScore.None, true, Type.Initial), Collections.emptyMap(), Collections.emptyList());
    private final int skillIncreases;
    private final Map<Integer, List<AbilitySlot>> advancementTable;

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
