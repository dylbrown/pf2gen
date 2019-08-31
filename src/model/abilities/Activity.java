package model.abilities;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;
import model.enums.Action;
import model.enums.Type;

import java.util.List;

public class Activity extends Ability {
    private final Action cost;
    private String trigger = "";

    public Activity(Action cost, int level, String name, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots, Type type, boolean multiple) {
        super(level, name, description, prerequisites, requiredAttrs, customMod, abilitySlots, type, multiple);
        this.cost = cost;
    }

    public Activity(Action cost, String trigger, int level, String name, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots, Type type, boolean multiple) {
        this(cost, level, name, description, prerequisites, requiredAttrs, customMod, abilitySlots, type, multiple);
        this.trigger = trigger;
    }

    public Action getCost() {
        return cost;
    }

    public String getTrigger() {
        return trigger;
    }
}
