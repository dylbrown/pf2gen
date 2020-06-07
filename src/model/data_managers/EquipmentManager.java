package model.data_managers;

import model.equipment.Equipment;
import model.equipment.armor.Armor;
import model.equipment.weapons.Weapon;
import model.equipment.weapons.WeaponGroup;
import model.xml_parsers.FileLoader;
import model.xml_parsers.equipment.ArmorLoader;
import model.xml_parsers.equipment.ItemLoader;
import model.xml_parsers.equipment.WeaponsLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static model.util.StringUtils.camelCase;

public class EquipmentManager {
    private static SortedSet<Equipment> allEquipment;
    private static final SortedSet<String> categories = new TreeSet<>();
    private static final WeaponsLoader weaponsLoader = new WeaponsLoader();
    private static final ArmorLoader armorLoader = new ArmorLoader();
    private static final Map<String, FileLoader<Equipment>> equipmentLoaders = new HashMap<>();

    static{
        String[] list = new File("data/equipment/").list();
        if(list != null) {
            for (String s : list) {
                if(!s.endsWith(".pfdyl")) continue;
                String niceName = camelCase(s.replace(".pfdyl", "").replaceAll("_", " "));
                if (!niceName.equals("Armor And Shields") && !niceName.equals("Weapons")) {
                    categories.add(niceName);
                    equipmentLoaders.put(niceName, new ItemLoader(s));
                }
            }
        } else {
            List<String> indexList = new ArrayList<>();
            try {
                URL index = new URL("https://dylbrown.github.io/pf2gen_data/data/equipment/index.txt"+ "?_=" + System.currentTimeMillis());
                URLConnection urlConnection = index.openConnection();
                urlConnection.setDefaultUseCaches(false);
                urlConnection.setUseCaches(false);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String temp;
                while((temp = bufferedReader.readLine()) != null)
                    indexList.add(temp+".pfdyl");
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String s : indexList) {
                String niceName = camelCase(s.replace(".pfdyl", "").replaceAll("_", " "));
                if (!niceName.equals("Armor And Shields") && !niceName.equals("Weapons")) {
                    categories.add(niceName);
                    equipmentLoaders.put(niceName, new ItemLoader(s));
                }
            }
        }
        categories.add("Armor And Shields");
        categories.add("Weapons");
    }

    private EquipmentManager() {}

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
