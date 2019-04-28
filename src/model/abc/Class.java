package model.abc;

import model.abilities.Ability;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilityScores.AbilityMod;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Class extends ABC {
    private final int skillIncreases;
    private final List<Ability> feats;
    private int hp;
    private Map<Integer, List<AbilitySlot>> advancementTable;

    public Class(String name, int hp, int skillIncreases, AbilityMod keyAbility, Map<Integer, List<AbilitySlot>> table, List<Ability> feats) {
        super(name, Collections.singletonList(keyAbility));
        this.hp = hp;
        this.skillIncreases = skillIncreases;
        advancementTable = table;
        this.feats = feats;
    }

    public List<AbilitySlot> getLevel(int level) {
        return Collections.unmodifiableList(advancementTable.get(level));
    }

    public int getHP() {
        return hp;
    }

    public int getSkillIncreases() {
        return skillIncreases;
    }

    public List<Ability> getFeats() {
        return Collections.unmodifiableList(feats);
    }
}
