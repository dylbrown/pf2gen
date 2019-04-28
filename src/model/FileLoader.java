package model;

import model.abc.Ancestry;
import model.abc.Background;
import model.abc.Class;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.Activity;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.ChoiceSlot;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.FilledSlot;
import model.abilityScores.AbilityMod;
import model.abilityScores.AbilityModChoice;
import model.abilityScores.AbilityScore;
import model.enums.*;
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

                String name = ""; int hp = 0; Size size = Size.Medium; int speed=0; String bonuses=""; String penalties=""; List<Ability> feats = new ArrayList<>();

                for(int i=0; i<classProperties.getLength(); i++) {
                    if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element curr = (Element) classProperties.item(i);
                    switch(curr.getTagName()){
                        case "Name":
                            name = curr.getTextContent().trim();
                            break;
                        case "HP":
                            hp = Integer.parseInt(curr.getTextContent().trim());
                            break;
                        case "Size":
                            size = Size.valueOf(camelCaseWord(curr.getTextContent().trim()));
                            break;
                        case "Speed":
                            speed = Integer.parseInt(curr.getTextContent().trim());
                            break;
                        case "AbilityBonuses":
                            bonuses = curr.getTextContent().trim();
                            break;
                        case "AbilityPenalties":
                            penalties = curr.getTextContent().trim();
                        case "Feats":
                            NodeList featNodes = curr.getElementsByTagName("Feat");
                            for(int j=0; j<featNodes.getLength(); j++) {
                                feats.add(makeAbility((Element) featNodes.item(i), ((Element) featNodes.item(i)).getAttribute("name")));
                            }
                    }
                }

                ancestries.add(new Ancestry(name, hp, size, speed, getAbilityMods(bonuses, penalties, Type.Ancestry), feats));
            }
        }
        return ancestries;
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
        return backgrounds;
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

                String name = ""; int hp = 0; int skillIncreases = 0; AbilityMod abilityMod = null; Map<Integer, List<AbilitySlot>> table = new HashMap<>(); List<Ability> feats = new ArrayList<>();

                for(int i=0; i<classProperties.getLength(); i++) {
                    if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element curr = (Element) classProperties.item(i);
                    switch(curr.getTagName()){
                        case "Name":
                            name = curr.getTextContent().trim();
                            break;
                        case "HP":
                            hp = Integer.parseInt(curr.getTextContent().trim());
                            break;
                        case "SkillIncreases":
                            skillIncreases = Integer.parseInt(curr.getTextContent().trim());
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
                                        abilitySlots.add(new FilledSlot(abilityName, makeAbility(temp, abilityName)));
                                        break;
                                    case "feat":
                                        abilitySlots.add(new FeatSlot(abilityName, getTypes(slotNode.getAttribute("type"))));
                                        break;
                                    case "choice":
                                        abilitySlots.add(new ChoiceSlot(abilityName, makeAbilities(slotNode.getChildNodes())));
                                        break;
                                }
                            }
                            table.put(level, abilitySlots);
                            break;
                        case "Feats":
                            NodeList featNodes = curr.getElementsByTagName("Ability");
                            for(int j=0; j<featNodes.getLength(); j++) {
                                feats.add(makeAbility((Element) featNodes.item(j), ((Element) featNodes.item(j)).getAttribute("name")));
                            }
                    }
                }
                classes.add(new Class(name, hp, skillIncreases, abilityMod, table, feats));
            }
        }
        return classes;
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
        boolean activity=false; Action cost = Action.One;
        if(element.getTagName().equals("Ability")) {
            List<AttributeMod> mods = new ArrayList<>();
            String description = "";
            for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                Node item = element.getChildNodes().item(i);
                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element propElem = (Element) item;
                String trim = propElem.getTextContent().trim();
                switch (propElem.getTagName()) {
                    case "Activity":
                        activity=true;
                        cost=Action.valueOf(camelCaseWord(propElem.getAttribute("cost").trim()));
                        break;
                    case "AttributeMods":
                        Proficiency prof = Proficiency.valueOf(camelCaseWord(propElem.getAttribute("Proficiency").trim()));
                        addMods(trim, prof, mods);
                        break;
                    case "Description":
                        description = trim;
                        break;
                }
            }
            if(activity)
                return new Activity(cost, name, description);
            else
                return new Ability(name, mods, description);
        }else if(element.getTagName().equals("AbilitySet")){
            String desc="";
            if(element.getElementsByTagName("Description").getLength() > 0)
                desc = element.getElementsByTagName("Description").item(0).getTextContent().trim();
            return new AbilitySet(name, desc, makeAbilities(element.getElementsByTagName("Ability")));
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
}
