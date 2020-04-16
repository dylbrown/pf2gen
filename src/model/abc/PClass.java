package model.abc;

import model.abilities.abilitySlots.AbilitySlot;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityScore;
import model.enums.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PClass extends AC {
    public static final PClass NO_CLASS;

    static{
        Builder builder = new Builder();
        builder.setName("No Class");
        builder.setAbilityMods(Collections.singletonList(new AbilityMod(AbilityScore.None, true, Type.Initial)));
        NO_CLASS = builder.build();
    }

    private final int skillIncreases;
    private final Map<Integer, List<AbilitySlot>> advancementTable;

    private PClass(PClass.Builder builder) {
        super(builder);
        this.skillIncreases = builder.skillIncreases;
        advancementTable = builder.advancementTable;
    }

    public List<AbilitySlot> getLevel(int level) {
        return Collections.unmodifiableList(advancementTable.get(level));
    }

    public int getSkillIncrease() {
        return skillIncreases;
    }

    public static class Builder extends AC.Builder {
        private int skillIncreases = 0;
        private Map<Integer, List<AbilitySlot>> advancementTable = Collections.emptyMap();

        public void setSkillIncreases(int skillIncreases) {
            this.skillIncreases = skillIncreases;
        }

        public void addToTable(int level, List<AbilitySlot> abilitySlots) {
            if(advancementTable.size() == 0) advancementTable = new TreeMap<>();
            advancementTable.put(level, abilitySlots);
        }

        public PClass build() {
            return new PClass(this);
        }
    }
}
