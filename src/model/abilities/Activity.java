package model.abilities;

import model.enums.Action;

import java.util.List;

public class Activity extends Ability {
    private final Action cost;
    private String trigger = "";

    public Activity(Action cost, int level, String name, String description, List<String> prerequisites) {
        super(level, name, description, prerequisites);
        this.cost = cost;
    }

    public Activity(Action cost, String trigger, int level, String name, String description, List<String> prerequisites) {
        this(cost, level, name, description, prerequisites);
        this.trigger = trigger;
    }
}
