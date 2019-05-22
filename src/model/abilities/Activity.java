package model.abilities;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;
import model.enums.Action;

import java.util.List;

public class Activity extends Ability {
    private final Action cost;
    private String trigger = "";

    public Activity(Action cost, int level, String name, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots) {
        super(level, name, description, prerequisites, requiredAttrs, customMod, abilitySlots);
        this.cost = cost;
    }

    public Activity(Action cost, String trigger, int level, String name, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots) {
        this(cost, level, name, description, prerequisites, requiredAttrs, customMod, abilitySlots);
        this.trigger = trigger;
    }

    public Action getCost() {
        return cost;
    }

    public String getTrigger() {
        return trigger;
    }
}
