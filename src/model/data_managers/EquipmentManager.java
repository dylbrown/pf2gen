package model.data_managers;

import model.equipment.Armor;
import model.equipment.Equipment;
import model.equipment.Weapon;
import model.equipment.WeaponGroup;
import model.xml_parsers.ArmorLoader;
import model.xml_parsers.WeaponsLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EquipmentManager {
    private static List<Equipment> allEquipment;
    private static WeaponsLoader weaponsLoader = new WeaponsLoader();
    private static ArmorLoader armorLoader = new ArmorLoader();

    public static List<Equipment> getEquipment() {
        if(allEquipment == null) {
            allEquipment = new ArrayList<>();
            allEquipment.addAll(getWeapons());
            allEquipment.addAll(getArmor());
        }
        return allEquipment;
    }

    public static Map<String, WeaponGroup> getWeaponGroups() {
        return weaponsLoader.getWeaponsGroups();
    }

    private static List<Armor> getArmor() {
        return armorLoader.parse();
    }

    private static List<Weapon> getWeapons() {
       return weaponsLoader.parse();
    }
}
