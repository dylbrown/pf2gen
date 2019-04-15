package model.abc;

import model.abilities.abilitySlots.AbilitySlot;
import model.abilityScores.AbilityMod;

import java.util.*;

public class Class extends ABC {
    private int hp;
    private Map<Integer, List<AbilitySlot>> advancementTable;

    public Class(String name, int hp, AbilityMod keyAbility, Map<Integer, List<AbilitySlot>> table) {
        super(name, Collections.singletonList(keyAbility));
        this.hp = hp;
        advancementTable = table;
    }

    public List<AbilitySlot> getLevel(int level) {
        return Collections.unmodifiableList(advancementTable.get(level));
    }

    public int getHP() {
        return hp;
    }
}
