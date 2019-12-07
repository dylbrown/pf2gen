package model.data_managers;

import model.equipment.Armor;
import model.equipment.Equipment;
import model.equipment.Weapon;
import model.equipment.WeaponGroup;
import model.xml_parsers.ArmorLoader;
import model.xml_parsers.WeaponsLoader;

import java.util.*;

public class EquipmentManager {
    private static SortedSet<Equipment> allEquipment;
    private static final WeaponsLoader weaponsLoader = new WeaponsLoader();
    private static final ArmorLoader armorLoader = new ArmorLoader();

    public static SortedSet<Equipment> getEquipment() {
        if(allEquipment == null) {
            allEquipment = new TreeSet<>((Comparator.comparing(Equipment::getName)));
            allEquipment.addAll(getWeapons());
            allEquipment.addAll(getArmor());
        }
        return Collections.unmodifiableSortedSet(allEquipment);
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
