package model.items.weapons;

public class WeaponGroup {
    private final String effect;
    private final String name;

    public WeaponGroup(String effect, String name) {
        this.effect = effect;
        this.name = name;
    }

    public String getEffect() {
        return effect;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
