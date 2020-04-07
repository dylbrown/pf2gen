package model.xml_parsers;

import model.attributes.Attribute;
import model.attributes.AttributeBonus;
import model.enums.Trait;
import model.enums.Type;
import model.equipment.Equipment;
import model.equipment.runes.ArmorRune;
import model.equipment.runes.Rune;
import model.equipment.runes.WeaponRune;
import model.equipment.weapons.Damage;
import model.equipment.weapons.DamageType;
import model.equipment.weapons.Dice;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static model.util.StringUtils.camelCase;
import static model.util.StringUtils.camelCaseWord;

public class ItemLoader extends FileLoader<Equipment> {
    private List<Equipment> items = null;
    private final String niceTitle;
    private static final ItemAbilityLoader abilityLoader = new ItemAbilityLoader();

    public ItemLoader(String s) {
        path = new File("data/equipment/"+s);
        niceTitle = camelCase(s.replace(".pfdyl","").replaceAll("_"," "));
    }
    @Override
    public List<Equipment> parse() {
        if(items == null) {
            items = new ArrayList<>();
            Document doc = getDoc(path);
            NodeList nodes = doc.getElementsByTagName("Item");
            for(int i=0; i<nodes.getLength(); i++) {
                if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) nodes.item(i);
                items.add(makeItem(curr));
            }
            nodes = doc.getElementsByTagName("WeaponRune");
            for(int i=0; i<nodes.getLength(); i++) {
                if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) nodes.item(i);
                items.add(makeWeaponRune(curr));
            }
            nodes = doc.getElementsByTagName("ArmorRune");
            for(int i=0; i<nodes.getLength(); i++) {
                if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) nodes.item(i);
                items.add(makeArmorRune(curr));
            }
        }
        return items;
    }

    private Equipment makeItem(Element item) {
        Equipment.Builder builder = new Equipment.Builder();
        builder.setCategory(niceTitle);
        Node parentNode = item.getParentNode();
        if(parentNode instanceof Element && ((Element) parentNode).getTagName().equals("SubCategory")) {
            builder.setSubCategory(((Element) parentNode).getAttribute("name"));
        }
        if(item.hasAttribute("level"))
            builder.setLevel(Integer.valueOf(item.getAttribute("level")));
        NodeList nodeList = item.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            parseTag(trim, curr, builder);
        }
        return builder.build();
    }


    private Equipment makeArmorRune(Element item) {
        ArmorRune.Builder builder = new ArmorRune.Builder();
        builder.setCategory(niceTitle);
        Node parentNode = item.getParentNode();
        if(parentNode instanceof Element && ((Element) parentNode).getTagName().equals("SubCategory")) {
            builder.setSubCategory(((Element) parentNode).getAttribute("name"));
        }
        if(item.hasAttribute("level"))
            builder.setLevel(Integer.valueOf(item.getAttribute("level")));
        if(item.hasAttribute("fundamental"))
            builder.setFundamental(true);
        NodeList nodeList = item.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            if ("BonusAC".equals(curr.getTagName())) {
                builder.setBonusAC(Integer.parseInt(trim));
            } else {
                parseRuneTag(trim, curr, builder);
            }
        }
        return builder.build();
    }

    private Equipment makeWeaponRune(Element item) {
        WeaponRune.Builder builder = new WeaponRune.Builder();
        builder.setCategory(niceTitle);
        Node parentNode = item.getParentNode();
        if(parentNode instanceof Element && ((Element) parentNode).getTagName().equals("SubCategory")) {
            builder.setSubCategory(((Element) parentNode).getAttribute("name"));
        }
        if(item.hasAttribute("level"))
            builder.setLevel(Integer.valueOf(item.getAttribute("level")));
        if(item.hasAttribute("fundamental"))
            builder.setFundamental(true);
        NodeList nodeList = item.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName()) {
                case "AttackBonus":
                    builder.setAttackBonus(Integer.parseInt(trim));
                    break;
                case "BonusDamage":
                    builder.setBonusDamage(parseDamage(trim));
                    break;
                default:
                    parseRuneTag(trim, curr, builder);
            }
        }
        return builder.build();
    }

    private Damage parseDamage(String trim) {
        DamageType damageType;
        String[] s = trim.split(" ");
        damageType = DamageType.valueOf(camelCaseWord(s[s.length-1]));
        String damage = trim.replaceAll("( \\z| [^ ]*\\z| persistent)", "");
        //TODO: Handle persistent
        if(damage.contains("d") && (damage.contains("+") || damage.contains("-"))) {
            int flip = (damage.contains("+")) ? 1 : -1;
            String[] damageSplit = damage.split("[+-]");
            String[] diceSplit = damageSplit[0].split("d");
            return new Damage(Dice.get(Integer.parseInt(diceSplit[0]), Integer.parseInt(diceSplit[1])),
                        flip * Integer.valueOf(damageSplit[1]),
                        damageType);
        }else if(damage.contains("d")) {
            String[] diceSplit = damage.trim().split("d");
            return new Damage(Dice.get(Integer.parseInt(diceSplit[0]), Integer.parseInt(diceSplit[1])),
                    0,
                    damageType);
        }else{
            return new Damage(Dice.get(0, 0),
                    Integer.parseInt(damage),
                    damageType);
        }
    }

    private void parseRuneTag(String trim, Element curr, Rune.Builder builder) {
        if ("GrantsProperties".equals(curr.getTagName())) {
            builder.setGrantsProperties(Integer.parseInt(trim));
        } else {
            parseTag(trim, curr, builder);
        }
    }

    static void parseTag(String trim, Element curr, Equipment.Builder builder) {
        switch (curr.getTagName()) {
            case "Name":
                builder.setName(trim);
                break;
            case "Description":
                builder.setDescription(trim);
                break;
            case "Price":
                builder.setValue(getPrice(trim));
                break;
            case "Bulk":
                if (trim.toUpperCase().equals("L"))
                    builder.setWeight(.1);
                else
                    builder.setWeight(Double.parseDouble(trim));
                break;
            case "Hands":
                builder.setHands(Integer.parseInt(trim));
                break;
            case "Traits":
                Arrays.stream(trim.split(",")).map((item)->
                {
                    try{
                        return Trait.valueOf(camelCaseWord(item.trim()));
                    }catch(IllegalArgumentException e){
                        System.out.println(e.getMessage());
                        return null;
                    }
                }).filter(Objects::nonNull)
                    .forEachOrdered(builder::addTrait);
                break;
            case "Bonuses":
                Arrays.stream(trim.split(",")).map((item)->{
                    String[] s = item.trim().split(" ");
                    if(s.length != 2) return null;
                    return new AttributeBonus(
                            Attribute.robustValueOf(s[1]),
                            Integer.parseInt(s[0]),
                            Type.Item);
                }).filter(Objects::nonNull)
                    .forEachOrdered(builder::addBonus);
                break;
            case "Ability":
                builder.addAbility(abilityLoader.makeAbility(curr));
                break;
        }
    }


    public static double getPrice(String priceString) {
        if(priceString.equals("")) return 0;
        String[] split = priceString.split(" ");
        double value = Double.parseDouble(split[0].replace(",",""));
        switch(split[1].toLowerCase()) {
            case "cp":
                value *= .1;
                break;
            case "gp":
                value *= 10;
                break;
            case "pp":
                value *= 100;
                break;
        }
        return value;
    }
}
