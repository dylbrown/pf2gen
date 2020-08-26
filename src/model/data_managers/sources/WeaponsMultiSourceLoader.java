package model.data_managers.sources;

import model.equipment.weapons.Weapon;
import model.equipment.weapons.WeaponGroup;
import model.xml_parsers.FileLoader;
import model.xml_parsers.equipment.WeaponsLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WeaponsMultiSourceLoader extends MultiSourceLoader<Weapon> {
    private Map<String, WeaponGroup> weaponGroups;

    public WeaponsMultiSourceLoader() {
        super("Weapon");
    }

    public Map<String, WeaponGroup> getWeaponGroups() {
        if(weaponGroups == null) {
            weaponGroups = new HashMap<>();
            for (FileLoader<Weapon> loader : loaders) {
                if (loader instanceof WeaponsLoader) {
                    weaponGroups.putAll(((WeaponsLoader) loader).getWeaponsGroups());
                }
            }
        }
        return Collections.unmodifiableMap(weaponGroups);
    }
}
