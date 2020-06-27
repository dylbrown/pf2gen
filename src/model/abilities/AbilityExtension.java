package model.abilities;

public abstract class AbilityExtension {
    private final Ability baseAbility;

    protected AbilityExtension(Ability baseAbility) {
        this.baseAbility = baseAbility;
    }

    public Ability getBaseAbility() {
        return baseAbility;
    }

    public static abstract class Builder {
        abstract AbilityExtension build(Ability baseAbility);
    }
}
