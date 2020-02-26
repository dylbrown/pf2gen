package model.xml_parsers;

import model.enums.DamageType;
import model.enums.Rarity;
import model.enums.WeaponProficiency;
import model.equipment.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

import static model.util.StringUtils.camelCase;
//TODO: Implement Builder Pattern
public class WeaponsLoader extends ItemLoader<Weapon> {

    private List<Weapon> weapons;
    private static final Map<String, WeaponGroup> weaponGroups = new HashMap<>();
    private static final Map<String, ItemTrait> weaponTraits = new HashMap<>();

    public WeaponsLoader() {
        path = new File("data/equipment/weapons.pfdyl");
    }

    public List<Weapon> parse() {
        if(weapons == null) {
            weapons = new ArrayList<>();
            Document doc = getDoc(path);
            NodeList groupNodes = doc.getElementsByTagName("WeaponGroup");
            for(int i=0; i<groupNodes.getLength(); i++) {
                if(groupNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) groupNodes.item(i);
                String name = curr.getElementsByTagName("Name").item(0).getTextContent().trim();
                String critEffect = curr.getElementsByTagName("CritEffect").item(0).getTextContent().trim();
                weaponGroups.put(name.toLowerCase(), new WeaponGroup(critEffect, name));
            }

            NodeList traitNodes = doc.getElementsByTagName("WeaponTrait");
            for(int i=0; i<traitNodes.getLength(); i++) {
                if(traitNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) traitNodes.item(i);
                String name = curr.getElementsByTagName("Name").item(0).getTextContent().trim();
                String desc = curr.getElementsByTagName("Description").item(0).getTextContent().trim();
                weaponTraits.put(camelCase(name), new ItemTrait(name, desc));
            }
            NodeList weaponNodes = doc.getElementsByTagName("Weapon");
            for(int i=0; i<weaponNodes.getLength(); i++) {
                if(weaponNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) weaponNodes.item(i);
                weapons.add(getWeapon(curr));
            }
        }
        return Collections.unmodifiableList(weapons);
    }

    static Weapon getWeapon(Element weapon) {
        double weight=0; double value=0; String name=""; String description = ""; Rarity rarity=Rarity.Common; Dice damage=Dice.get(1,6); DamageType damageType = DamageType.Piercing; int hands = 1; WeaponGroup group = null; List<ItemTrait> traits = new ArrayList<>(); WeaponProficiency weaponProficiency; int range=0; int reload=0; boolean isRanged=false; boolean uncommon=false;
        Node proficiencyNode= weapon.getParentNode();
        Node rangeNode = proficiencyNode.getParentNode();
        if(rangeNode.getNodeName().equals("Ranged"))
            isRanged = true;


        if(weapon.hasAttribute("Uncommon") || weapon.hasAttribute("uncommon"))
            uncommon = true;

        weaponProficiency = WeaponProficiency.valueOf(camelCase(proficiencyNode.getNodeName()));
        NodeList nodeList = weapon.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName()) {
                case "Name":
                    name = trim;
                    break;
                case "Description":
                    description = trim;
                    break;
                case "Price":
                    value = getPrice(trim);
                    break;
                case "Damage":
                    String[] split = trim.split(" ");
                    String[] diceSplit = split[0].split("d");
                    damage = Dice.get(Integer.parseInt(diceSplit[0]), Integer.parseInt(diceSplit[1]));
                    switch(split[1].toUpperCase()) {
                        case "B":
                            damageType = DamageType.Bludgeoning;
                            break;
                        case "P":
                            damageType = DamageType.Piercing;
                            break;
                        case "S":
                            damageType = DamageType.Slashing;
                            break;
                    }
                    break;
                case "Range":
                    range = Integer.parseInt(trim.split(" ")[0]);
                    break;
                case "Reload":
                    reload = Integer.parseInt(trim);
                    break;
                case "Bulk":
                    if (trim.toUpperCase().equals("L"))
                        weight = .1;
                    else
                        weight = Double.parseDouble(trim);
                    break;
                case "Hands":
                    hands = Integer.parseInt(trim);
                    break;
                case "Group":
                    group = weaponGroups.get(trim.toLowerCase());
                    break;
                case "Traits":
                    Arrays.stream(trim.split(",")).map((item)->{
                        String[] s = item.trim().split(" ", 2);
                        if(s.length == 1)
                            return weaponTraits.get(camelCase(s[0]));
                        else
                            return new SpecialItemTrait(weaponTraits.get(camelCase(s[0])), s[1]);
                    }).forEachOrdered(traits::add);
                    break;
            }
        }
        if(isRanged)
            return new RangedWeapon(weight, value, name, description, rarity, damage, damageType, hands, group, traits, weaponProficiency, range, reload, uncommon);
        else
            return new Weapon(weight, value, name, description, rarity, damage, damageType, hands, group, traits, weaponProficiency, uncommon);
    }

    public Map<String, WeaponGroup> getWeaponsGroups() {
        if(weapons == null) parse();
        return Collections.unmodifiableMap(weaponGroups);
    }
}
