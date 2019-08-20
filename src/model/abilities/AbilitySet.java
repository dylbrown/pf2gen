package model.abilities;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;
import model.enums.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbilitySet extends Ability {
    private final List<Ability> abilities;
    public AbilitySet(int level, String name, String desc, List<Ability> abilities, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots, Type type) {
        super(level, name, desc, prerequisites, requiredAttrs, customMod, abilitySlots, type);
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
