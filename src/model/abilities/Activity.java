package model.abilities;

import model.enums.Action;

public class Activity extends Ability {
    private final Action cost;

    public Activity(Action cost, String name, String description) {
        super(name, description);
        this.cost = cost;
    }
}
