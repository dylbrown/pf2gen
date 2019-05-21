package model;

import model.abc.Ancestry;
import model.abc.Background;
import model.abc.Class;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.Activity;
import model.abilities.SkillIncrease;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.ChoiceSlot;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.FilledSlot;
import model.abilityScores.AbilityMod;
import model.abilityScores.AbilityModChoice;
import model.abilityScores.AbilityScore;
import model.enums.*;
import model.equipment.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileLoader {
    private static final File ancestriesPath = new File("data/ancestries");
    private static final File backgroundsPath = new File("data/backgrounds");
    private static final File classesPath = new File("data/classes");
    private static FileLoader instance;
    private List<Ancestry> ancestries;
    private List<Background> backgrounds;
    private List<Class> classes;
    private List<Weapon> weapons;
    private List<Armor> armorAndShields;
    private Map<String, WeaponGroup> weaponGroups = new HashMap<>();
    private Map<String, ItemTrait> weaponTraits = new HashMap<>();
    private Map<String, ItemTrait> armorTraits = new HashMap<>();
    private DocumentBuilder builder;

    static{
        instance = new FileLoader();
        try {
            instance.builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        assert instance.builder != null;
    }

    public static List<Ancestry> getAncestries() {
        return instance.getAnc();
    }
    public static List<Background> getBackgrounds() {
        return instance.getBack();
    }
    public static List<Class> getClasses() {
        return instance.getCls();
    }
    public static List<Weapon> getWeapons() {return instance.getWeaps();}
    public static List<Armor> getArmorAndShields() {return instance.getArmor();}

    private List<Ancestry> getAnc() {
        if(ancestries == null) {
            ancestries = new ArrayList<>();
            for (File file : Objects.requireNonNull(ancestriesPath.listFiles())) {
                Document doc = null;
                try {
                    doc = builder.parse(file);
                } catch (IOException | SAXException e) {
                    e.printStackTrace();
                }
                assert doc != null;
                NodeList classProperties = doc.getElementsByTagName("ancestry").item(0).getChildNodes();

                String name = ""; int hp = 0; Size size = Size.Medium; int speed=0; String bonuses=""; String penalties=""; List<Ability> feats = new ArrayList<>();List<Ability> heritages = new ArrayList<>(); String description = ""; List<Language> languages = new ArrayList<>();List<Language> bonusLanguages = new ArrayList<>();

                for(int i=0; i<classProperties.getLength(); i++) {
                    if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element curr = (Element) classProperties.item(i);
                    String trim = curr.getTextContent().trim();
                    switch(curr.getTagName()){
                        case "Name":
                            name = trim;
                            break;
                        case "Description":
                            description = trim;
                            break;
                        case "HP":
                            hp = Integer.parseInt(trim);
                            break;
                        case "Languages":
                            for (String s : trim.split(",")) {
                                languages.add(Language.valueOf(s.trim()));
                            }
                            break;
                        case "BonusLanguages":
                            for (String s : trim.split(",")) {
                                if(s.trim().equals("All")){
                                    bonusLanguages.addAll(Arrays.asList(Language.getChooseable()));
                                    break;
                                }
                                bonusLanguages.add(Language.valueOf(s.trim()));
                            }
                            break;
                        case "Size":
                            size = Size.valueOf(camelCaseWord(trim));
                            break;
                        case "Speed":
                            speed = Integer.parseInt(trim);
                            break;
                        case "AbilityBonuses":
                            bonuses = trim;
                            break;
                        case "AbilityPenalties":
                            penalties = trim;
                        case "Feats":
                            NodeList[] nodeLists = {curr.getElementsByTagName("Ability"),curr.getElementsByTagName("AbilitySet")};
                            for (NodeList featNodes : nodeLists) {
                                for (int j = 0; j < featNodes.getLength(); j++) {
                                    if (((Element) featNodes.item(j)).getAttribute("type").trim().toLowerCase().equals("heritage")) {
                                        heritages.add(makeAbility((Element) featNodes.item(j), ((Element) featNodes.item(j)).getAttribute("name")));
                                    } else {
                                        feats.add(makeAbility((Element) featNodes.item(j), ((Element) featNodes.item(j)).getAttribute("name")));
                                    }
                                }
                            }
                    }
                }

                ancestries.add(new Ancestry(name, description, languages, bonusLanguages, hp, size, speed, getAbilityMods(bonuses, penalties, Type.Ancestry), feats, heritages));
            }
        }
        return Collections.unmodifiableList(ancestries);
    }

    private List<Background> getBack() {
        if(backgrounds == null) {
            backgrounds = new ArrayList<>();
            for (File file : Objects.requireNonNull(backgroundsPath.listFiles())) {
                Document doc = null;
                try {
                    doc = builder.parse(file);
                } catch (IOException | SAXException e) {
                    e.printStackTrace();
                }
                assert doc != null;
                NodeList classProperties = doc.getElementsByTagName("background").item(0).getChildNodes();

                String name = "";
                String desc = "";
                Attribute skill=null;
                String data="";
                String bonuses="";

                for(int i=0; i<classProperties.getLength(); i++) {
                    if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element curr = (Element) classProperties.item(i);
                    switch(curr.getTagName()){
                        case "Name":
                            name = curr.getTextContent().trim();
                            break;
                        case "Description":
                            desc = curr.getTextContent().trim();
                            break;
                        case "Skill":
                            if(camelCaseWord(curr.getTextContent().trim()).substring(0, 4).equals("Lore")) {
                                skill = Attribute.Lore;
                                data = camelCaseWord(curr.getTextContent().trim()).substring(4).trim().replaceAll("[()]", "");
                            }else{
                                skill = Attribute.valueOf(camelCaseWord(curr.getTextContent().trim()));
                            }
                            break;
                        case "AbilityBonuses":
                            bonuses = curr.getTextContent().trim();
                            break;
                    }
                }
                backgrounds.add(new Background(name, bonuses, desc, getAbilityMods(bonuses, "", Type.Background), skill, data));
            }
        }
        return Collections.unmodifiableList(backgrounds);
    }

    private List<Class> getCls() {
        if(classes == null) {
            classes = new ArrayList<>();
            for (File file : Objects.requireNonNull(classesPath.listFiles())) {
                Document doc = null;
                try {
                    doc = builder.parse(file);
                } catch (IOException | SAXException e) {
                    e.printStackTrace();
                }
                assert doc != null;
                NodeList classProperties = doc.getElementsByTagName("class").item(0).getChildNodes();

                String name = ""; int hp = 0; int skillIncreases = 0; AbilityMod abilityMod = null; Map<Integer, List<AbilitySlot>> table = new HashMap<>(); List<Ability> feats = new ArrayList<>(); String description="";

                for(int i=0; i<classProperties.getLength(); i++) {
                    if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element curr = (Element) classProperties.item(i);
                    String trim = curr.getTextContent().trim();
                    switch(curr.getTagName()){
                        case "Name":
                            name = trim;
                            break;
                        case "Description":
                            description = trim;
                            break;
                        case "HP":
                            hp = Integer.parseInt(trim);
                            break;
                        case "SkillIncreases":
                            skillIncreases = Integer.parseInt(trim);
                            break;
                        case "AbilityChoices":
                            if(curr.getElementsByTagName("AbilityScore").getLength() == 1) {
                                abilityMod = new AbilityMod(AbilityScore.valueOf(camelCaseWord(curr.getElementsByTagName("AbilityScore").item(0).getTextContent())), true, Type.Class);
                            }else{
                                List<AbilityScore> scores = new ArrayList<>();
                                for(int j=0; j<curr.getElementsByTagName("AbilityScore").getLength(); j++) {
                                    scores.add(AbilityScore.valueOf(camelCaseWord(curr.getElementsByTagName("AbilityScore").item(j).getTextContent())));
                                }
                                abilityMod = new AbilityModChoice(scores, Type.Class);
                            }
                            break;
                        case "FeatureList":
                            int level = Integer.parseInt(curr.getAttribute("level"));
                            List<AbilitySlot> abilitySlots = new ArrayList<>();
                            for(int j=0; j<curr.getElementsByTagName("AbilitySlot").getLength(); j++) {
                                if(curr.getElementsByTagName("AbilitySlot").item(j).getNodeType() != Node.ELEMENT_NODE)
                                    continue;
                                Element slotNode = (Element) curr.getElementsByTagName("AbilitySlot").item(j);
                                String abilityName = slotNode.getAttribute("name");
                                switch(slotNode.getAttribute("state").toLowerCase().trim()){
                                    case "filled":
                                        Element temp = (Element) slotNode.getElementsByTagName("Ability").item(0);
                                        abilitySlots.add(new FilledSlot(abilityName, level, makeAbility(temp, abilityName)));
                                        break;
                                    case "feat":
                                        abilitySlots.add(new FeatSlot(abilityName, level, getTypes(slotNode.getAttribute("type"))));
                                        break;
                                    case "choice":
                                        abilitySlots.add(new ChoiceSlot(abilityName, level, makeAbilities(slotNode.getChildNodes())));
                                        break;
                                }
                            }
                            table.put(level, abilitySlots);
                            break;
                        case "Feats":
                            NodeList[] nodeLists = {curr.getElementsByTagName("Ability"),curr.getElementsByTagName("AbilitySet")};
                            for (NodeList featNodes : nodeLists) {
                                for (int j = 0; j < featNodes.getLength(); j++) {
                                    feats.add(makeAbility((Element) featNodes.item(j), ((Element) featNodes.item(j)).getAttribute("name")));
                                }
                            }
                    }
                }
                classes.add(new Class(name, description, hp, skillIncreases, abilityMod, table, feats));
            }
        }
        return Collections.unmodifiableList(classes);
    }

    private List<Ability> makeAbilities(NodeList nodes) {
        List<Ability> choices = new ArrayList<>();
        for(int i=0; i<nodes.getLength(); i++) {
            if(!(nodes.item(i) instanceof Element)) continue;
            Element item = (Element) nodes.item(i);
            Ability name = makeAbility(item, item.getAttribute("name"));
            if(name != null)
                choices.add(name);
        }
        return choices;
    }

    private List<Type> getTypes(String string) {
        List<Type> results = new ArrayList<>();
        String[] split = string.trim().split(" ");
        for(String term: split) {
            if(!term.trim().equals(""))
                results.add(Type.valueOf(camelCaseWord(term.trim())));
        }
        return results;
    }

    private Ability makeAbility(Element element, String name) {
        boolean activity=false; Action cost = Action.One; String trigger = ""; List<String> prerequisites = new ArrayList<>();int level = 1;
        if(element.getTagName().equals("Ability")) {
            List<AttributeMod> mods = new ArrayList<>();
            String description = "";
            if(!element.getAttribute("cost").equals("")) {
                activity=true;
                cost=Action.robustValueOf(camelCaseWord(element.getAttribute("cost").trim()));
            }
            if(!element.getAttribute("level").equals("")) {
                level=Integer.parseInt(element.getAttribute("level"));
            }
            for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                Node item = element.getChildNodes().item(i);
                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element propElem = (Element) item;
                String trim = propElem.getTextContent().trim();
                switch (propElem.getTagName()) {
                    case "Trigger":
                        trigger = trim;
                        break;
                    case "AttributeMods":
                        Proficiency prof = Proficiency.valueOf(camelCaseWord(propElem.getAttribute("Proficiency").trim()));
                        addMods(trim, prof, mods);
                        break;
                    case "Description":
                        description = trim;
                        break;
                    case "Prerequisites":
                        prerequisites.addAll(Arrays.asList(trim.split(",")));
                        break;
                }
            }
            if(cost == Action.Reaction)
                return new Activity(cost, trigger, level, name, description, prerequisites);
            else if(activity)
                return new Activity(cost, level, name, description, prerequisites);
            else if(element.getAttribute("skillIncrease").equals("true"))
                return new SkillIncrease(level, name, description, prerequisites);
            else
                return new Ability(level, name, mods, description, prerequisites);
        }else if(element.getTagName().equals("AbilitySet")){
            String desc="";
            if(element.getElementsByTagName("Description").getLength() > 0)
                desc = element.getElementsByTagName("Description").item(0).getTextContent().trim();
            if(element.getElementsByTagName("Prerequisites").getLength() > 0)
                prerequisites.addAll(Arrays.asList(element.getElementsByTagName("Prerequisites").item(0).getTextContent().trim().split(",")));
            return new AbilitySet(level, name, desc, makeAbilities(element.getElementsByTagName("Ability")),prerequisites);
        }
        return null;
    }

    private void addMods(String textContent, Proficiency prof, List<AttributeMod> mods) {
        String[] split = textContent.split(",");
        for(String str: split) {
            if (!str.trim().equals(""))
                mods.add(new AttributeMod(Attribute.valueOf(camelCase(str.trim()).replaceAll(" ", "")), prof));
        }
        //TODO: Handle Lore
    }

    private List<AbilityMod> getAbilityMods(String bonuses, String penalties, Type type) {
        List<AbilityMod> abilityMods = new ArrayList<>();

        String[] split = bonuses.split(",");
        for(int i=0;i<split.length;i++) {
            split[i] = split[i].trim();
            if(split[i].equals("")) continue;
            String[] eachScore = split[i].split("or");
            if(eachScore.length == 1) {
                AbilityScore abilityScore = AbilityScore.valueOf(camelCaseWord(split[i]));
                if(abilityScore != AbilityScore.Free)
                    abilityMods.add(new AbilityMod(abilityScore, true, type));
                else
                    abilityMods.add(new AbilityModChoice(type));
            }else{
                abilityMods.add(new AbilityModChoice(Arrays.asList(parseChoices(eachScore)), type));
            }

        }

        split = penalties.split(",");
        for(int i=0;i<split.length;i++) {
            if(split[i].trim().equals("")) continue;
            split[i] = camelCaseWord(split[i].trim());
            abilityMods.add(new AbilityMod(AbilityScore.valueOf(split[i]), false, type));
        }

        return abilityMods;
    }

    private AbilityScore[] parseChoices(String[] eachScore) {
        AbilityScore[] scores = new AbilityScore[eachScore.length];
        for(int i=0;i<eachScore.length;i++) {
            scores[i] = AbilityScore.valueOf(camelCaseWord(eachScore[i].trim()));
        }
        return scores;
    }

    private String camelCase(String str) {
        String[] split = str.split(" ");
        for(int i=0; i<split.length;i++) {
            split[i] = camelCaseWord(split[i]);
        }
        return String.join(" ", split);
    }

    private String camelCaseWord(String str) {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private List<Weapon> getWeaps() {
        if(weapons == null) {
            weapons = new ArrayList<>();
            Document doc = null;
            try {
                doc = builder.parse(new File("data/equipment/weapons.pfdyl"));
            } catch (IOException | SAXException e) {
                e.printStackTrace();
            }
            assert doc != null;
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

    private Weapon getWeapon(Element weapon) {
        double weight=0; double value=0; String name=""; String description = ""; Rarity rarity=Rarity.Common; Dice damage=Dice.get(1,6); DamageType damageType = DamageType.Piercing; int hands = 1; WeaponGroup group = null; List<ItemTrait> traits = new ArrayList<>(); WeaponProficiency weaponProficiency; int range=0; int reload=0; boolean isRanged=false;
        Node proficiencyNode= weapon.getParentNode();
        Node rangeNode = proficiencyNode.getParentNode();
        if(rangeNode.getNodeName().equals("Ranged"))
            isRanged = true;

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
                    String[] split = trim.split(" ");
                    value = Double.parseDouble(split[0]);
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
                    break;
                case "Damage":
                    split = trim.split(" ");
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
                    Arrays.stream(trim.split(",")).map((item)->weaponTraits.get(camelCase(item.trim().split(" ")[0]))).forEachOrdered(traits::add);
                    break;
            }
        }
        if(isRanged)
            return new RangedWeapon(weight, value, name, description, rarity, damage, damageType, hands, group, traits, weaponProficiency, range, reload);
        else
            return new Weapon(weight, value, name, description, rarity, damage, damageType, hands, group, traits, weaponProficiency);
    }

    private List<Armor> getArmor() {
        if(armorAndShields == null) {
            armorAndShields = new ArrayList<>();
            Document doc = null;
            try {
                doc = builder.parse(new File("data/equipment/armorAndShields.pfdyl"));
            } catch (IOException | SAXException e) {
                e.printStackTrace();
            }
            assert doc != null;

            NodeList traitNodes = doc.getElementsByTagName("ArmorTrait");
            for(int i=0; i<traitNodes.getLength(); i++) {
                if(traitNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) traitNodes.item(i);
                String name = curr.getElementsByTagName("Name").item(0).getTextContent().trim();
                String desc = curr.getElementsByTagName("Description").item(0).getTextContent().trim();
                armorTraits.put(name, new ItemTrait(name, desc));
            }

            NodeList armorNodes = doc.getElementsByTagName("Armor");
            for(int i=0; i<armorNodes.getLength(); i++) {
                if(armorNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) armorNodes.item(i);
                armorAndShields.add(getArmor(curr));
            }
        }
        return Collections.unmodifiableList(armorAndShields);
    }

    private Armor getArmor(Element armor) {
        double weight=0; double value=0; String name=""; String description = ""; Rarity rarity=Rarity.Common; List<ItemTrait> traits = new ArrayList<>(); boolean isShield=false; int acMod=0; int tacMod=0; int maxDex=0; int acp=0; int speedPenalty=0;
        Node proficiencyNode= armor.getParentNode();
        if(proficiencyNode.getNodeName().trim().equals("Shield"))
            isShield = true;
        NodeList nodeList = armor.getChildNodes();
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
                case "AC":
                    acMod = Integer.parseInt(trim.replaceAll("\\+",""));
                    break;
                case "TAC":
                    tacMod = Integer.parseInt(trim.replaceAll("\\+",""));
                    break;
                case "MaxDex":
                    maxDex = Integer.parseInt(trim.replaceAll("\\+",""));
                    break;
                case "ACP":
                    acp = Integer.parseInt(trim.replaceAll("\\+",""));
                    break;
                case "SpeedPenalty":
                    speedPenalty = Integer.parseInt(trim.replaceAll("\\+",""));
                    break;
                case "Price":
                    String[] split = trim.split(" ");
                    value = Double.parseDouble(split[0]);
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
                    break;
                case "Bulk":
                    if (trim.toUpperCase().equals("L"))
                        weight = .1;
                    else
                        weight = Double.parseDouble(trim);
                    break;
                case "Traits":
                    Arrays.stream(trim.split(",")).map((item)->armorTraits.get(camelCase(item.trim().split(" ")[0]))).forEachOrdered(traits::add);
                    break;
            }
        }
        if(isShield)
            return new Shield(weight, value, name, description, rarity, acMod, tacMod, maxDex, acp, speedPenalty, traits);
        else
            return new Armor(weight, value, name, description, rarity, acMod, tacMod, maxDex, acp, speedPenalty, traits);
    }
}
