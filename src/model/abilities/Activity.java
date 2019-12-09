package model.abilities;

import model.enums.Action;

public class Activity extends Ability {
    private final Action cost;
    private final String trigger;

    public Activity(Activity.Builder builder) {
        super(builder);
        this.cost = builder.cost;
        this.trigger = builder.trigger;
    }

    public Action getCost() {
        return cost;
    }

    public String getTrigger() {
        return trigger;
    }

    public static class Builder extends Ability.Builder {
        private Action cost;
        private String trigger;

        public void setCost(Action cost) {
            this.cost = cost;
        }

        public void setTrigger(String trigger) {
            this.trigger = trigger;
        }

        @Override
        public Activity build() {
            return new Activity(this);
        }
    }
}
