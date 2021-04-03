package model.abilities;

import model.ability_scores.AbilityMod;
import model.attributes.AttributeMod;
import model.enums.Sense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static model.util.Copy.copy;

public class GranterExtension extends AbilityExtension {
    private final List<AttributeMod> modifiers;
    private final List<AbilityMod> abilityMods;
    private final int skillIncreases;
    private final List<Sense> senses;

    private GranterExtension(GranterExtension.Builder builder, Ability baseAbility) {
        super(baseAbility);
        this.modifiers = builder.modifiers;
        this.abilityMods = builder.abilityMods;
        this.skillIncreases = builder.skillIncreases;
        this.senses = builder.senses;
    }

    public List<AttributeMod> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    public List<AbilityMod> getAbilityMods() {
        return Collections.unmodifiableList(abilityMods);
    }

    public List<Sense> getSenses() {
        return Collections.unmodifiableList(senses);
    }

    public int getSkillIncreases() {
        return skillIncreases;
    }

    public static class Builder extends AbilityExtension.Builder {
        private List<AttributeMod> modifiers = Collections.emptyList();
        private List<AbilityMod> abilityMods = Collections.emptyList();
        private List<Sense> senses = Collections.emptyList();
        private int skillIncreases = 0;

        public Builder() {}

        public Builder(GranterExtension.Builder other) {
            this.modifiers = copy(other.modifiers);
            this.abilityMods = copy(other.abilityMods);
            this.skillIncreases = other.skillIncreases;
            this.senses = copy(other.senses);
        }

        public void setAttrMods(List<AttributeMod> modifiers) {
            this.modifiers = modifiers;
        }

        public void setBoosts(List<AbilityMod> abilityMods) {
            this.abilityMods = abilityMods;
        }

        public void addAllMods(List<AttributeMod> mods) {
            if(modifiers.size() == 0) modifiers = new ArrayList<>();
            modifiers.addAll(mods);
        }

        public void setSkillIncreases(int skillIncreases) {
            this.skillIncreases = skillIncreases;
        }

        public void addSense(Sense sense) {
            if(senses.size() == 0) senses = new ArrayList<>();
            senses.add(sense);
        }

        @Override
        public GranterExtension build(Ability baseAbility) {
            return new GranterExtension(this, baseAbility);
        }
    }
}
