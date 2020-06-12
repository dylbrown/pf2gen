package model.xml_parsers.equipment;

import model.attributes.Attribute;
import model.attributes.AttributeBonus;
import model.data_managers.sources.SourceConstructor;
import model.enums.Trait;
import model.enums.Type;
import model.equipment.Equipment;
import model.equipment.runes.ArmorRune;
import model.equipment.runes.Rune;
import model.equipment.runes.WeaponRune;
import model.equipment.weapons.Damage;
import model.equipment.weapons.DamageType;
import model.equipment.weapons.Dice;
import model.util.StringUtils;
import model.xml_parsers.FileLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static model.util.StringUtils.camelCaseWord;

public class EquipmentLoader extends FileLoader<Equipment> {
    private static final ItemAbilityLoader abilityLoader = new ItemAbilityLoader(null, null);

    public EquipmentLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }


    @Override
    protected Equipment parseItem(File file, Element item) {
        String niceName = StringUtils.unclean(file.getName());
        switch(StringUtils.clean(item.getTagName())) {
            case "weaponrune": return makeWeaponRune(niceName, item);
            case "armorrune": return makeArmorRune(niceName, item);
        }
        return makeItem(niceName, item);
    }

    private Equipment makeItem(String niceName, Element item) {
        Equipment.Builder builder = new Equipment.Builder();
        builder.setCategory(niceName);
        Node parentNode = item.getParentNode();
        if(parentNode instanceof Element && ((Element) parentNode).getTagName().equals("SubCategory")) {
            builder.setSubCategory(((Element) parentNode).getAttribute("name"));
        }
        if(item.hasAttribute("level"))
            builder.setLevel(Integer.parseInt(item.getAttribute("level")));
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


    private Equipment makeArmorRune(String niceName, Element item) {
        ArmorRune.Builder builder = new ArmorRune.Builder();
        builder.setCategory(niceName);
        Node parentNode = item.getParentNode();
        if(parentNode instanceof Element && ((Element) parentNode).getTagName().equals("SubCategory")) {
            builder.setSubCategory(((Element) parentNode).getAttribute("name"));
        }
        if(item.hasAttribute("level"))
            builder.setLevel(Integer.parseInt(item.getAttribute("level")));
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

    private Equipment makeWeaponRune(String niceName, Element item) {
        WeaponRune.Builder builder = new WeaponRune.Builder();
        builder.setCategory(niceName);
        Node parentNode = item.getParentNode();
        if(parentNode instanceof Element && ((Element) parentNode).getTagName().equals("SubCategory")) {
            builder.setSubCategory(((Element) parentNode).getAttribute("name"));
        }
        if(item.hasAttribute("level"))
            builder.setLevel(Integer.parseInt(item.getAttribute("level")));
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
                case "WeaponDice":
                    builder.setBonusWeaponDice(Integer.parseInt(trim));
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
        boolean persistent = trim.contains("persistent");
        if(damage.contains("d") && (damage.contains("+") || damage.contains("-"))) {
            int flip = (damage.contains("+")) ? 1 : -1;
            String[] damageSplit = damage.split("[+-]");
            String[] diceSplit = damageSplit[0].split("d");
            return new Damage.Builder().addDice(Dice.get(Integer.parseInt(diceSplit[0]), Integer.parseInt(diceSplit[1])))
                    .addAmount(flip * Integer.parseInt(damageSplit[1]))
                    .setDamageType(damageType)
                    .setPersistent(persistent)
                    .build();
        }else if(damage.contains("d")) {
            String[] diceSplit = damage.trim().split("d");
            return new Damage.Builder().addDice(Dice.get(Integer.parseInt(diceSplit[0]), Integer.parseInt(diceSplit[1])))
                    .setDamageType(damageType)
                    .setPersistent(persistent).build();
        }else{
            return new Damage.Builder().addAmount(Integer.parseInt(damage))
                    .setDamageType(damageType)
                    .setPersistent(persistent).build();
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
