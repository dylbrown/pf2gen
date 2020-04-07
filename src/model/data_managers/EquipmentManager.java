package model.data_managers;

import model.equipment.armor.Armor;
import model.equipment.Equipment;
import model.equipment.weapons.Weapon;
import model.equipment.weapons.WeaponGroup;
import model.xml_parsers.ArmorLoader;
import model.xml_parsers.FileLoader;
import model.xml_parsers.ItemLoader;
import model.xml_parsers.WeaponsLoader;

import java.io.File;
import java.util.*;

import static model.util.StringUtils.camelCase;

public class EquipmentManager {
    private static SortedSet<Equipment> allEquipment;
    private static final SortedSet<String> categories = new TreeSet<>();
    private static final WeaponsLoader weaponsLoader = new WeaponsLoader();
    private static final ArmorLoader armorLoader = new ArmorLoader();
    private static final Map<String, FileLoader<Equipment>> equipmentLoaders = new HashMap<>();

    static{
        for (String s : Objects.requireNonNull(new File("data/equipment/").list())) {
            String niceName = camelCase(s.replace(".pfdyl", "").replaceAll("_", " "));
            if(!niceName.equals("Armor And Shields") && !niceName.equals("Weapons")) {
                categories.add(niceName);
                equipmentLoaders.put(niceName, new ItemLoader(s));
            }
        }
        categories.add("Armor And Shields");
        categories.add("Weapons");
    }

    public static SortedSet<Equipment> getEquipment() {
        if(allEquipment == null) {
            allEquipment = new TreeSet<>((Comparator.comparing(Equipment::getName)));
            allEquipment.addAll(getWeapons());
            allEquipment.addAll(getArmor());
            for (FileLoader<Equipment> loader : equipmentLoaders.values()) {
                allEquipment.addAll(loader.parse());
            }

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

    public static SortedSet<String> getCategories() {
        return Collections.unmodifiableSortedSet(categories);
    }

    public static List<Equipment> getItems(String category) {
        if(category.equals("Armor And Shields")) return armorLoader.getEquipmentList();
        if(category.equals("Weapons")) return weaponsLoader.getEquipmentList();
        FileLoader<Equipment> loader = equipmentLoaders.get(category);
        if(loader == null) return Collections.emptyList();
        return Collections.unmodifiableList(loader.parse());
    }
}
