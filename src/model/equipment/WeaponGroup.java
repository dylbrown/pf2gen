package model.equipment;

public class WeaponGroup {
    private final String critEffect;
    private final String name;

    public WeaponGroup(String critEffect, String name) {
        this.critEffect = critEffect;
        this.name = name;
    }

    public String getCritEffect() {
        return critEffect;
    }

    public String getName() {
        return name;
    }
}
