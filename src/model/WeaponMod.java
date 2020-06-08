package model;

import model.enums.Proficiency;

import java.util.Objects;

public class WeaponMod {
    private final String weaponName;
    private final Proficiency proficiency;

    public WeaponMod(String weaponName, Proficiency proficiency) {
        this.weaponName = weaponName;
        this.proficiency = proficiency;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public Proficiency getProficiency() {
        return proficiency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeaponMod weaponMod = (WeaponMod) o;
        return Objects.equals(weaponName, weaponMod.weaponName) &&
                proficiency == weaponMod.proficiency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(weaponName, proficiency);
    }
}
