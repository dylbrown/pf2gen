package model.xml_parsers;

import model.AttributeMod;
import model.AttributeModSingleChoice;
import model.abilities.*;
import model.abilities.abilitySlots.*;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.data_managers.AllSpells;
import model.data_managers.EquipmentManager;
import model.enums.Action;
import model.enums.Attribute;
import model.enums.Proficiency;
import model.enums.Type;
import model.spells.CasterType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.misc.IOUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static model.util.StringUtils.camelCase;
import static model.util.StringUtils.camelCaseWord;

abstract class FileLoader<T> {
    File path;
    private static final DocumentBuilder builder;

    static{
        DocumentBuilder builder1;
        try {
            builder1 = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            builder1 = null;
        }
        builder = builder1;
        assert builder != null;
        EquipmentManager.getEquipment();//TODO: Separate out loading weapon groups
    }

    public abstract List<T> parse();

    protected abstract Type getSource();

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

    List<Document> getDocs(File path) {
        List<Document> results = new ArrayList<>();
        if(path.exists()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                if(file.getName().substring(file.getName().length()-5).equals("pfdyl"))
                    results.add(getDoc(file));
            }

        }else{
            try {
                URL index = new URL("https://dylbrown.github.io/pf2gen_data/"+path.toString().replaceAll("\\\\", "/")+"/index.txt"+ "?_=" + System.currentTimeMillis() );
                URLConnection urlConnection = index.openConnection();
                urlConnection.setDefaultUseCaches(false);
                urlConnection.setUseCaches(false);
                String s = new String(IOUtils.readFully(urlConnection.getInputStream(), -1, true), StandardCharsets.UTF_8);
                for (String name : s.split("\\n")) {
                    results.add(getDoc(new File(path.toString()+"\\"+name+".pfdyl")));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return results;
    }


    List<Ability> makeAbilities(NodeList nodes) {
        List<Ability> choices = new ArrayList<>();
        for(int i=0; i<nodes.getLength(); i++) {
            if(!(nodes.item(i) instanceof Element)) continue;
            Element item = (Element) nodes.item(i);
            if(!item.getTagName().matches("Ability(Set)?")) continue;
            Ability ability = makeAbility(item, item.getAttribute("name"));
            if(ability != null)
                choices.add(ability);
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
        if(element.getTagName().contains("Ability")) {
            Ability.Builder builder;
            Activity.Builder acBuilder = null;
            AttackAbility.Builder attBuilder = null;
            SpellAbility.Builder spBuilder = null;
            if(!element.getAttribute("cost").equals("")) {
                acBuilder = new Activity.Builder();
                acBuilder.setCost(Action.robustValueOf(camelCaseWord(element.getAttribute("cost").trim())));
                builder = acBuilder;
            }else builder = new Ability.Builder();
            builder.setName(name);
            if(!element.getAttribute("level").equals("")) {
                builder.setLevel(Integer.parseInt(element.getAttribute("level")));
            }else builder.setLevel(level);
            String increase = element.getAttribute("skillIncrease");
            if(!increase.equals("")){
                if(increase.equals("true")) builder.setSkillIncreases(1);
                else builder.setSkillIncreases(Integer.parseInt(increase));
            }
            if(!element.getAttribute("abilityBoosts").trim().equals("")) {
                int count = Integer.parseInt(element.getAttribute("abilityBoosts"));
                builder.setBoosts(getBoosts(count, level));
            }
            if(element.getAttribute("multiple").equals("true"))
                builder.setMultiple(true);
            for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                Node item = element.getChildNodes().item(i);
                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element propElem = (Element) item;
                String trim = propElem.getTextContent().trim();
                switch (propElem.getTagName()) {//TODO: Support SpellsKnown, Spell tags
                    case "Trigger":
                        if (acBuilder != null) {
                            acBuilder.setTrigger(trim);
                        }
                        break;
                    case "AttributeMods":
                        Proficiency prof = Proficiency.valueOf(camelCaseWord(propElem.getAttribute("Proficiency").trim()));                     builder.addAllMods(addMods(trim, prof));
                        break;
                    case "Description":
                        builder.setDescription(trim);
                        break;
                    case "Prerequisites":
                        builder.setPrerequisites(Arrays.asList(trim.split(",")));
                        break;
                    case "Requires":
                        if(trim.matches(".*\\d.*"))
                            break;
                        requiredAttrs.addAll(Arrays.stream(trim.split(",")).map((str)->{
                            String[] split = str.split(" [iI]n ");
                            Proficiency reqProf = Proficiency.valueOf(split[0].trim());
                            if(camelCaseWord(split[1].trim().substring(0, 4)).equals("Lore")) {
                                String data = camelCase(str.trim().substring(4).trim().replaceAll("[()]", ""));
                                return new AttributeMod(Attribute.Lore, reqProf, data);
                            }else{
                                return new AttributeMod(Attribute.robustValueOf(camelCase(split[1].trim())), reqProf);
                            }
                        }).collect(Collectors.toCollection(ArrayList::new)));
                        break;
                    case "Weapon":
                        if(attBuilder == null) {
                            builder = attBuilder = new AttackAbility.Builder(builder);
                        }
                        attBuilder.addWeapon(WeaponsLoader.getWeapon(propElem));
                        break;
                    case "CustomMod":
                        builder.setCustomMod(trim);
                        break;
                    case "SpellSlots":
                        if(spBuilder == null) {
                            builder = spBuilder = new SpellAbility.Builder(builder);
                        }
                        spBuilder.addSpellSlots(Integer.parseInt(propElem.getAttribute("level")),
                                              Integer.parseInt(propElem.getAttribute("count")));
                        if(!propElem.getAttribute("type").equals("")) {
                            spBuilder.setCasterType(CasterType.valueOf(propElem.getAttribute("type")));
                        }
                        break;
                    case "SpellsKnown":
                        if(spBuilder == null) {
                            builder = spBuilder = new SpellAbility.Builder(builder);
                        }
                        spBuilder.addExtraSpellKnown(Integer.parseInt(propElem.getAttribute("level")),
                                                    Integer.parseInt(propElem.getAttribute("count")));
                        break;
                    case "Spell":
                        if(spBuilder == null) {
                            builder = spBuilder = new SpellAbility.Builder(builder);
                        }
                        spBuilder.addFocusSpell(AllSpells.find(propElem.getAttribute("name")));
                        break;
                    case "AbilitySlot":
                        String abilityName = propElem.getAttribute("name");
                        int slotLevel = level;
                        if(!propElem.getAttribute("level").equals(""))
                            slotLevel = Integer.parseInt(propElem.getAttribute("level").toLowerCase().trim());
                        switch(propElem.getAttribute("state").toLowerCase().trim()){
                            case "filled":
                                NodeList ability = propElem.getElementsByTagName("Ability");
                                if(ability.getLength() > 0) {
                                    Element temp = (Element) ability.item(0);
                                    builder.addAbilitySlot(new FilledSlot(abilityName,
                                            slotLevel, makeAbility(temp, abilityName, level)));
                                }else{
                                    String type = propElem.getAttribute("type");
                                    if(type.equals("")) type = "General";
                                    builder.addAbilitySlot(new DynamicFilledSlot(abilityName, slotLevel,
                                            propElem.getAttribute("contents"),
                                            getDynamicType(type), false));
                                }
                                break;
                            case "feat":
                                builder.addAbilitySlot(new FeatSlot(abilityName, slotLevel,
                                        getTypes(propElem.getAttribute("type"))));
                                break;
                            case "choice":
                                builder.addAbilitySlot(new SingleChoiceSlot(abilityName, slotLevel,
                                        makeAbilities(propElem.getChildNodes())));
                                break;
                        }
                }
            }
            builder.setType(getSource());
            if(element.getTagName().equals("AbilitySet")){
                AbilitySet.Builder setBuilder = new AbilitySet.Builder(builder);
                setBuilder.setAbilities(makeAbilities(element.getChildNodes()));
                builder = setBuilder;
            }
            return builder.build();
        }
        return null;
    }

    Type getDynamicType(String type) {
        return Type.valueOf(type.trim().replaceAll(" [fF]eat", ""));
    }

    private List<AbilityMod> getBoosts(int count, int level) {
        List<AbilityMod> boosts = new ArrayList<>(count);
        for(int i=0; i<count; i++)
            boosts.add(new AbilityModChoice(Type.get(level)));
        return boosts;
    }

    private List<AttributeMod> addMods(String textContent, Proficiency prof) {
        List<AttributeMod> mods = new ArrayList<>();
        String[] split = textContent.split(",");
        for(String str: split) {
            if (!str.trim().equals("")) {
                String[] orCheck = str.trim().split(" or ");
                if(orCheck.length > 1) {
                    mods.add(new AttributeModSingleChoice(Attribute.robustValueOf(orCheck[0]),
                            Attribute.robustValueOf(orCheck[1]), prof));
                }else {
                    if (camelCaseWord(str.trim()).substring(0, 4).equals("Lore")) {
                        Attribute skill = Attribute.Lore;
                        String data = camelCase(str.trim().substring(4).trim().replaceAll("[()]", ""));
                        mods.add(new AttributeMod(skill, prof, data));
                    } else {
                        mods.add(new AttributeMod(Attribute.robustValueOf(str), prof));
                    }
                }
            }
        }
        return mods;
    }

    List<AbilityMod> getAbilityMods(String bonuses, String penalties, Type type) {
        List<AbilityMod> abilityMods = new ArrayList<>();

        String[] split = bonuses.split(",");
        for(int i=0;i<split.length;i++) {
            split[i] = split[i].trim();
            if(split[i].equals("")) continue;
            abilityMods.add(getAbilityBonus(split[i], type));
        }

        split = penalties.split(",");
        for(int i=0;i<split.length;i++) {
            if(split[i].trim().equals("")) continue;
            split[i] = camelCaseWord(split[i].trim());
            abilityMods.add(new AbilityMod(AbilityScore.valueOf(split[i]), false, type));
        }

        return abilityMods;
    }

    AbilityMod getAbilityBonus(String abilityString, Type type) {
        String[] eachScore = abilityString.split("or|,");
        if(eachScore.length == 1) {
            AbilityScore abilityScore = AbilityScore.valueOf(camelCaseWord(abilityString));
            if(abilityScore == AbilityScore.Free)
                return new AbilityModChoice(type);
            else
                return new AbilityMod(abilityScore, true, type);
        }else{
            return new AbilityModChoice(Arrays.asList(parseChoices(eachScore)), type);
        }
    }

    private AbilityScore[] parseChoices(String[] eachScore) {
        AbilityScore[] scores = new AbilityScore[eachScore.length];
        for(int i=0;i<eachScore.length;i++) {
            scores[i] = AbilityScore.valueOf(camelCaseWord(eachScore[i].trim()));
        }
        return scores;
    }
}
