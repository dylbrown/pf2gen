package model.abilities;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbilitySet extends Ability {
    private final List<Ability> abilities;
    public AbilitySet(int level, String name, String desc, List<Ability> abilities, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots) {
        super(level, name, desc, prerequisites, requiredAttrs, customMod, abilitySlots);
        this.abilities = abilities;
        modifiers = new ArrayList<>();
        for(Ability ability: abilities) {
            modifiers.addAll(ability.getModifiers());
        }
    }

    public List<Ability> getAbilities(){
        return Collections.unmodifiableList(abilities);
    }
}
