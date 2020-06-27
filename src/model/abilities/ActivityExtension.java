package model.abilities;

import model.enums.Action;

public class ActivityExtension extends AbilityExtension {
    private final Action cost;
    private final String trigger;

    public ActivityExtension(ActivityExtension.Builder builder, Ability baseAbility) {
        super(baseAbility);
        this.cost = builder.cost;
        this.trigger = builder.trigger;
    }

    public Action getCost() {
        return cost;
    }

    public String getTrigger() {
        return trigger;
    }

    public static class Builder extends AbilityExtension.Builder {
        private Action cost = Action.Free;
        private String trigger = "";

        Builder() {}

        public void setCost(Action cost) {
            this.cost = cost;
        }

        public void setTrigger(String trigger) {
            this.trigger = trigger;
        }

        @Override
        public ActivityExtension build(Ability baseAbility) {
            return new ActivityExtension(this, baseAbility);
        }
    }
}
