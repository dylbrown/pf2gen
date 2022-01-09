package model.abc;

import model.abilities.Ability;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityScore;
import model.ability_slots.AbilitySlot;
import model.data_managers.sources.Source;
import model.enums.Type;
import model.util.ObjectNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PClass extends AC {
    public static final PClass NO_CLASS;

    static{
        Builder builder = new Builder(null);
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

    public Ability findClassFeature(String contents) throws ObjectNotFoundException {
        for (List<AbilitySlot> list : advancementTable.values()) {
            for (AbilitySlot slot : list) {
                if(slot.isPreSet() && slot.getName().equalsIgnoreCase(contents))
                    return slot.getCurrentAbility();
            }
        }
        throw new ObjectNotFoundException(contents, getName()+ " Class Feature");
    }

    public static class Builder extends AC.Builder {
        private int skillIncreases = 0;
        private Map<Integer, List<AbilitySlot>> advancementTable = Collections.emptyMap();

        public Builder(Source source) {
            super(source);
        }

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
