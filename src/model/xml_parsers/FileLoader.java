package model.xml_parsers;

import model.AttributeMod;
import model.AttributeModChoice;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.Activity;
import model.abilities.SkillIncrease;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.ChoiceSlot;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.FilledSlot;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.enums.Action;
import model.enums.Attribute;
import model.enums.Proficiency;
import model.enums.Type;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

abstract class FileLoader<T> {
    File path;
    private static DocumentBuilder builder = null;

    static{
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        assert builder != null;
    }

    public abstract List<T> parse();

    Document getDoc(File path) {
        Document doc = null;
        if(path.exists()) {
            try {
                doc = builder.parse(path);
            } catch (IOException | SAXException e) {
                e.printStackTrace();
            }
        }else{
            try {
                URL url = new URL("https://dylbrown.github.io/pf2gen_data/"+path.toString().replaceAll("\\\\", "/"));
                System.out.println("Could not find "+path.getName()+" on disk, loading from repository.");
                doc=builder.parse(url.openStream());
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
        }
        assert doc != null;
        return doc;
    }


    List<Ability> makeAbilities(NodeList nodes) {
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

    List<Type> getTypes(String string) {
        List<Type> results = new ArrayList<>();
        String[] split = string.trim().split(" ");
        for(String term: split) {
            if(!term.trim().equals(""))
                results.add(Type.valueOf(camelCaseWord(term.trim())));
        }
        return results;
    }
    Ability makeAbility(Element element, String name) {
        return makeAbility(element, name, 1);
    }
    Ability makeAbility(Element element, String name, int level) {
        boolean activity=false; Action cost = Action.One; String trigger = ""; List<String> prerequisites = new ArrayList<>(); List<AttributeMod> requiredAttrs = new ArrayList<>(); String customMod = ""; List<AbilitySlot> abilitySlots = new ArrayList<>();
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
                    case "Requires":
                        requiredAttrs.addAll(Arrays.stream(trim.split(",")).map((str)->{
                            String[] split = str.split(" in ");
                            Proficiency reqProf = Proficiency.valueOf(split[0].trim());
                            if(camelCaseWord(split[1].trim().substring(0, 4)).equals("Lore")) {
                                String data = camelCase(str.trim().substring(4).trim().replaceAll("[()]", ""));
                                return new AttributeMod(Attribute.Lore, reqProf, data);
                            }else{
                                return new AttributeMod(Attribute.valueOf(camelCase(split[1].trim())), reqProf);
                            }
                        }).collect(Collectors.toCollection(ArrayList::new)));
                        break;
                    case "CustomMod":
                        customMod = trim;
                        break;
                    case "AbilitySlot":
                        String abilityName = propElem.getAttribute("name");
                        switch(propElem.getAttribute("state").toLowerCase().trim()){
                            case "filled":
                                Element temp = (Element) propElem.getElementsByTagName("Ability").item(0);
                                abilitySlots.add(new FilledSlot(abilityName, level, makeAbility(temp, abilityName, level)));
                                break;
                            case "feat":
                                abilitySlots.add(new FeatSlot(abilityName, level, getTypes(propElem.getAttribute("type"))));
                                break;
                            case "choice":
                                abilitySlots.add(new ChoiceSlot(abilityName, level, makeAbilities(propElem.getChildNodes())));
                                break;
                        }
                }
            }
            if(cost == Action.Reaction)
                return new Activity(cost, trigger, level, name, description, prerequisites, requiredAttrs, customMod, abilitySlots);
            else if(activity)
                return new Activity(cost, level, name, description, prerequisites, requiredAttrs, customMod, abilitySlots);
            else if(element.getAttribute("skillIncrease").equals("true"))
                return new SkillIncrease(level, name, description, prerequisites, requiredAttrs, customMod, abilitySlots);
            else if(!element.getAttribute("abilityBoosts").trim().equals("")) {
                int count = Integer.parseInt(element.getAttribute("abilityBoosts"));
                return new Ability(level, name, getBoosts(count, level), description, requiredAttrs, customMod, abilitySlots);
            }else
                return new Ability(level, name, mods, description, prerequisites, requiredAttrs, customMod, abilitySlots);
        }else if(element.getTagName().equals("AbilitySet")){
            String desc="";
            if(element.getElementsByTagName("Description").getLength() > 0)
                desc = element.getElementsByTagName("Description").item(0).getTextContent().trim();
            if(element.getElementsByTagName("Prerequisites").getLength() > 0)
                prerequisites.addAll(Arrays.asList(element.getElementsByTagName("Prerequisites").item(0).getTextContent().trim().split(",")));
            return new AbilitySet(level, name, desc, makeAbilities(element.getElementsByTagName("Ability")),prerequisites, requiredAttrs, customMod, abilitySlots);
        }
        return null;
    }

    private List<AbilityMod> getBoosts(int count, int level) {
        List<AbilityMod> boosts = new ArrayList<>(count);
        for(int i=0; i<count; i++)
            boosts.add(new AbilityModChoice(Type.get(level)));
        return boosts;
    }

    private void addMods(String textContent, Proficiency prof, List<AttributeMod> mods) {
        String[] split = textContent.split(",");
        for(String str: split) {
            if (!str.trim().equals("")) {
                String[] orCheck = str.trim().split(" or ");
                if(orCheck.length > 1) {
                    mods.add(new AttributeModChoice(Attribute.valueOf(camelCase(orCheck[0]).replaceAll(" ", "")),
                            Attribute.valueOf(camelCase(orCheck[1]).replaceAll(" ", "")), prof));
                }else {
                    if (camelCaseWord(str.trim()).substring(0, 4).equals("Lore")) {
                        Attribute skill = Attribute.Lore;
                        String data = camelCase(str.trim().substring(4).trim().replaceAll("[()]", ""));
                        mods.add(new AttributeMod(skill, prof, data));
                    } else {
                        mods.add(new AttributeMod(Attribute.valueOf(camelCase(str.trim()).replaceAll(" ", "")), prof));
                    }
                }
            }
        }
    }

    List<AbilityMod> getAbilityMods(String bonuses, String penalties, Type type) {
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

    String camelCase(String str) {
        String[] split = str.split(" ");
        for(int i=0; i<split.length;i++) {
            split[i] = camelCaseWord(split[i]);
        }
        return String.join(" ", split);
    }

    String camelCaseWord(String str) {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
