package model.items.weapons;

import model.enums.Proficiency;

public class WeaponGroupMod {
    private final WeaponGroup group;
    private final Proficiency proficiency;

    public WeaponGroupMod(WeaponGroup group, Proficiency proficiency) {
        this.group = group;
        this.proficiency = proficiency;
    }

    public WeaponGroup getGroup() {
        return group;
    }

    public Proficiency getProficiency() {
        return proficiency;
    }
}
