package model.abilities;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;

import java.util.List;

public class SkillIncrease extends Ability {
    public SkillIncrease(int level, String name, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots) {
        super(level, name, description, prerequisites, requiredAttrs, customMod, abilitySlots);
    }
}
