package model.abilities;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;
import model.enums.Type;

import java.util.List;

public class SkillIncrease extends Ability {
    public SkillIncrease(int level, String name, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots) {
        super(level, name, description, prerequisites, requiredAttrs, customMod, abilitySlots, Type.None);
    }
}
