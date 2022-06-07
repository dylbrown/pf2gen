package model.abilities;

import model.enums.Action;

public class ActivityExtension extends AbilityExtension {
    private final Action cost;
    private final String trigger;
    private final String frequency;

    public ActivityExtension(ActivityExtension.Builder builder, Ability baseAbility) {
        super(baseAbility);
        this.cost = builder.cost;
        this.trigger = builder.trigger;
        this.frequency = builder.frequency;
    }

    public Action getCost() {
        return cost;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getFrequency() {
        return frequency;
    }

    public static class Builder extends AbilityExtension.Builder {
        private Action cost = Action.Free;
        private String trigger = "";
        private  String frequency = "";

        Builder() {}

        public Builder(Builder other) {
            cost = other.cost;
            trigger = other.trigger;
            frequency = other.frequency;
        }

        public void setCost(Action cost) {
            this.cost = cost;
        }

        public void setTrigger(String trigger) {
            this.trigger = trigger;
        }

        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }

        @Override
        public ActivityExtension build(Ability baseAbility) {
            return new ActivityExtension(this, baseAbility);
        }
    }
}
