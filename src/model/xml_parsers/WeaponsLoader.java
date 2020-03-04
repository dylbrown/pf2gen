package model.xml_parsers;

import model.enums.DamageType;
import model.enums.WeaponProficiency;
import model.equipment.*;
import model.equipment.weapons.Dice;
import model.equipment.weapons.RangedWeapon;
import model.equipment.weapons.Weapon;
import model.equipment.weapons.WeaponGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static model.util.StringUtils.camelCase;
//TODO: Implement Builder Pattern
public class WeaponsLoader extends FileLoader<Weapon> {

    private List<Weapon> weapons;
    private List<Equipment> equipment;
    private static final Map<String, WeaponGroup> weaponGroups = new HashMap<>();
    private static final Map<String, CustomTrait> weaponTraits = new HashMap<>();

    public WeaponsLoader() {
        path = new File("data/equipment/weapons.pfdyl");
    }

    public List<Weapon> parse() {
        if(weapons == null) {
            weapons = new ArrayList<>();
            Document doc = getDoc(path);

            iterateElements(doc, "WeaponGroup", (curr)->{
                String name = curr.getElementsByTagName("Name").item(0).getTextContent().trim();
                String critEffect = curr.getElementsByTagName("CritEffect").item(0).getTextContent().trim();
                weaponGroups.put(name.toLowerCase(), new WeaponGroup(critEffect, name));
            });

            iterateElements(doc, "WeaponTrait", (curr)->{
                String name = curr.getElementsByTagName("Name").item(0).getTextContent().trim();
                String desc = curr.getElementsByTagName("Description").item(0).getTextContent().trim();
                weaponTraits.put(camelCase(name), new CustomTrait(name, desc));
            });

            iterateElements(doc, "Weapon", (curr)-> weapons.add(getWeapon(curr)));
        }
        return Collections.unmodifiableList(weapons);
    }

    static Weapon getWeapon(Element weapon) {
        Weapon.Builder builder = new Weapon.Builder();
        RangedWeapon.Builder rangedBuilder = null;
        Node proficiencyNode= weapon.getParentNode();
        Node rangeNode = proficiencyNode.getParentNode();
        if(rangeNode.getNodeName().equals("Ranged"))
            builder = rangedBuilder = new RangedWeapon.Builder(builder);


        if(weapon.hasAttribute("Uncommon") || weapon.hasAttribute("uncommon"))
            builder.setUncommon(true);

        builder.setProficiency(WeaponProficiency.valueOf(camelCase(proficiencyNode.getNodeName())));
        NodeList nodeList = weapon.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName()) {
                case "Damage":
                    String[] split = trim.split(" ");
                    String[] diceSplit = split[0].split("d");
                    builder.setDamage(Dice.get(Integer.parseInt(diceSplit[0]), Integer.parseInt(diceSplit[1])));
                    switch(split[1].toUpperCase()) {
                        case "B":
                            builder.setDamageType(DamageType.Bludgeoning);
                            break;
                        case "P":
                            builder.setDamageType(DamageType.Piercing);
                            break;
                        case "S":
                            builder.setDamageType(DamageType.Slashing);
                            break;
                    }
                    break;
                case "Range":
                    if(rangedBuilder == null)
                        builder = rangedBuilder = new RangedWeapon.Builder(builder);
                    rangedBuilder.setRange(Integer.parseInt(trim.split(" ")[0]));
                    break;
                case "Reload":
                    if(rangedBuilder == null)
                        builder = rangedBuilder = new RangedWeapon.Builder(builder);
                    rangedBuilder.setReload(Integer.parseInt(trim));
                    break;
                case "Bulk":
                    if (trim.toUpperCase().equals("L"))
                        builder.setWeight(.1);
                    else
                        builder.setWeight(Double.parseDouble(trim));
                    break;
                case "Group":
                    builder.setGroup(weaponGroups.get(trim.toLowerCase()));
                    break;
                case "Traits":
                    Arrays.stream(trim.split(",")).map((item)->{
                        String[] s = item.trim().split(" ", 2);
                        if(s.length == 1)
                            return weaponTraits.get(camelCase(s[0]));
                        else
                            return new SpecialCustomTrait(weaponTraits.get(camelCase(s[0])), s[1]);
                    }).forEachOrdered(builder::addWeaponTrait);
                    break;
                default:
                    ItemLoader.parseTag(trim, curr, builder);
                    break;
            }
        }
        return (rangedBuilder != null) ? rangedBuilder.build() : builder.build();
    }

    public Map<String, WeaponGroup> getWeaponsGroups() {
        if(weapons == null) parse();
        return Collections.unmodifiableMap(weaponGroups);
    }

    public List<Equipment> getEquipmentList() {
        if(equipment == null) equipment = parse().stream().map(a->(Equipment) a).collect(Collectors.toList());
        return equipment;
    }
}
