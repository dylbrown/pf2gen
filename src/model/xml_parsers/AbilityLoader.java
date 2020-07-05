package model.xml_parsers;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import model.abilities.*;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.ability_slots.*;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.attributes.AttributeModSingleChoice;
import model.data_managers.sources.SourceConstructor;
import model.data_managers.sources.SourcesLoader;
import model.enums.Action;
import model.enums.Proficiency;
import model.enums.Trait;
import model.enums.Type;
import model.spells.CasterType;
import model.spells.SpellType;
import model.spells.Tradition;
import model.xml_parsers.equipment.WeaponsLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static model.util.StringUtils.camelCase;
import static model.util.StringUtils.camelCaseWord;

public abstract class AbilityLoader<T> extends FileLoader<T> {

    protected static List<DynamicFilledSlot> dynSlots;

    public AbilityLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }

   protected static Map<Class<? extends AbilityLoader<?>>, Function<Element, Type>> sources = new HashMap<>();

    private ReadOnlyStringWrapper spellListName;
    private final Map<String, ReadOnlyStringWrapper> archetypeSpellListNames = new HashMap<>();

    @Override
    protected void clearAccumulators() {
        spellListName = new ReadOnlyStringWrapper();
        archetypeSpellListNames.clear();
    }

    protected List<Ability> makeAbilities(NodeList nodes) {
        List<Ability> choices = new ArrayList<>();
        for(int i=0; i<nodes.getLength(); i++) {
            if(!(nodes.item(i) instanceof Element)) continue;
            Element item = (Element) nodes.item(i);
            if(!item.getTagName().matches("Ability(Set)?")) continue;
            Ability ability = makeAbility(item, item.getAttribute("name")).build();
            if(ability != null)
                choices.add(ability);
        }
        return choices;
    }

    public static List<String> getTypes(String string) {
        List<String> results = new ArrayList<>();
        String[] split = string.trim().split(" ");
        for(String term: split) {
            if(!term.trim().equals(""))
                results.add(camelCaseWord(term.trim()));
        }
        return results;
    }
    protected Ability.Builder makeAbility(Element element, String name) {
        return makeAbility(element, name, 1);
    }

    protected Ability.Builder makeAbility(Element element, String name, int level) {
        Ability.Builder builder;
        // Load a template
        Node firstChild = element.getFirstChild();
        if(firstChild != null) {
            while(firstChild.getNodeType() != Node.ELEMENT_NODE) {
                firstChild = firstChild.getNextSibling();
            }
            if(((Element) firstChild).getTagName().equals("Template"))
                builder = SourcesLoader.instance().templates().find(firstChild.getTextContent().trim()).get();
            else builder = new Ability.Builder();
        } else builder = new Ability.Builder();
        if(!element.getAttribute("cost").equals("")) {
            builder.getExtension(ActivityExtension.Builder.class)
                    .setCost(Action.robustValueOf(element.getAttribute("cost")));
        }
        builder.setName(name);
        if(!element.getAttribute("page").equals(""))
            builder.setPageNo(Integer.parseInt(element.getAttribute("page")));
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
            switch (propElem.getTagName()) {
                case "Archetype":
                    builder.getExtension(ArchetypeExtension.Builder.class).setArchetype(trim);
                    break;
                case "Trigger":
                    builder.getExtension(ActivityExtension.Builder.class).setTrigger(trim);
                    break;
                case "AttributeMods":
                    Proficiency prof = Proficiency.valueOf(camelCaseWord(propElem.getAttribute("Proficiency").trim()));
                    builder.addAllMods(addMods(trim, prof));
                    break;
                case "Description":
                    builder.setDescription(trim);
                    break;
                case "Prerequisites":
                    builder.setPrerequisites(Arrays.asList(trim.split(", ?")));
                    break;
                case "PrereqStrings":
                    builder.setPrereqStrings(Arrays.asList(trim.split(", ?")));
                    break;
                case "GivesPrerequisites":
                    builder.setGivesPrerequisites(Arrays.asList(trim.split(", ?")));
                    break;
                case "Requirements":
                    builder.setRequirements(trim);
                    break;
                case "Requires":
                    if(trim.matches(".*\\d.*"))
                        break;
                    builder.setRequiredAttrs(Arrays.stream(trim.split(",")).map((str)->{
                        String[] split = str.split(" [iI]n ");
                        Proficiency reqProf = Proficiency.valueOf(camelCaseWord(split[0].trim()));
                        if(camelCaseWord(split[1].trim().substring(0, 4)).equals("Lore")) {
                            String data = camelCase(str.trim().substring(4).trim().replaceAll("[()]", ""));
                            return new AttributeMod(Attribute.Lore, reqProf, data);
                        }else{
                            return new AttributeMod(Attribute.robustValueOf(camelCase(split[1].trim())), reqProf);
                        }
                    }).collect(Collectors.toCollection(ArrayList::new)));
                    break;
                case "Weapon":
                    builder.getExtension(AttackExtension.Builder.class)
                            .addWeapon(WeaponsLoader.getWeapon(propElem));
                    break;
                case "CustomMod":
                    builder.setCustomMod(trim);
                    break;
                case "Spellcasting":
                    SpellExtension.Builder spellExt = builder.getExtension(SpellExtension.Builder.class);
                    String spellListName = propElem.getAttribute("listName");
                    String type = propElem.getAttribute("type");
                    String tradition = propElem.getAttribute("tradition");
                    String ability = propElem.getAttribute("ability");
                    if(spellListName != null)
                        spellExt.setSpellListName(spellListName);
                    else System.out.println("Warning: missing spellListName");
                    if(type != null && !type.toLowerCase().equals("") && !type.toLowerCase().equals("focusonly"))
                        spellExt.setCasterType(CasterType.valueOf(propElem.getAttribute("type")));
                    if(tradition != null && !tradition.equals(""))
                        spellExt.setTradition(Tradition.valueOf(tradition));
                    if(ability != null && !ability.equals(""))
                        spellExt.setCastingAbility(AbilityScore.robustValueOf(ability));
                    break;
                case "SpellSlots":
                    builder.getExtension(SpellExtension.Builder.class)
                            .addSpellSlots(Integer.parseInt(propElem.getAttribute("level")),
                            Integer.parseInt(propElem.getAttribute("count")));
                    break;
                case "SpellsKnown":
                    builder.getExtension(SpellExtension.Builder.class)
                            .addExtraSpellKnown(Integer.parseInt(propElem.getAttribute("level")),
                            Integer.parseInt(propElem.getAttribute("count")));
                    break;
                case "Spell":
                    SpellType spellType = null;
                    switch(propElem.getAttribute("type")) {
                        case "Spell": spellType = SpellType.Spell; break;
                        case "Cantrip": spellType = SpellType.Cantrip; break;
                        case "Focus": spellType = SpellType.Focus; break;
                        case "Focus Cantrip": spellType = SpellType.FocusCantrip; break;
                    }
                    builder.getExtension(SpellExtension.Builder.class)
                            .addBonusSpell(spellType, SourcesLoader.instance()
                            .spells().find(propElem.getAttribute("name")));
                    break;
                case "AbilitySlot":
                    builder.addAbilitySlot(makeAbilitySlot(propElem, level));
                    break;
                case "Traits":
                    for (String s : trim.split(" ?, ?")) {
                        Trait trait = Trait.valueOf(camelCaseWord(s.trim()));
                        builder.addTraits(trait);
                        if(trait == Trait.Dedication)
                            builder.getExtension(ArchetypeExtension.Builder.class)
                                    .setDedication(true);
                    }
            }
        }
        Function<Element, Type> function = sources.get(this.getClass());
        if(function != null)
            builder.setType(function.apply(element));
        if(element.getTagName().equals("AbilitySet")){
            builder.getExtension(AbilitySetExtension.Builder.class)
                    .setAbilities(makeAbilities(element.getChildNodes()));
        }
        if(builder.hasExtension(SpellExtension.Builder.class)) {
            SpellExtension.Builder spells = builder.getExtension(SpellExtension.Builder.class);
            if(spells.getListName() == null || spells.getListName().equals("")) {
                if(builder.hasExtension(ArchetypeExtension.Builder.class)) {
                    ArchetypeExtension.Builder archetype = builder.getExtension(ArchetypeExtension.Builder.class);
                    ReadOnlyStringProperty readOnlyProperty = archetypeSpellListNames
                            .computeIfAbsent(archetype.getArchetype(), s -> new ReadOnlyStringWrapper())
                            .getReadOnlyProperty();
                    if(readOnlyProperty.get() == null || readOnlyProperty.get().equals(""))
                        spells.setSpellListName(readOnlyProperty);
                    else
                        spells.setSpellListName(readOnlyProperty.get());
                } else {
                    ReadOnlyStringProperty readOnlyProperty = spellListName.getReadOnlyProperty();
                    if(readOnlyProperty.get() == null || readOnlyProperty.get().equals(""))
                        spells.setSpellListName(readOnlyProperty);
                    else
                        spells.setSpellListName(readOnlyProperty.get());
                }
            } else {
                if(builder.hasExtension(ArchetypeExtension.Builder.class)) {
                    ArchetypeExtension.Builder archetype = builder.getExtension(ArchetypeExtension.Builder.class);
                    archetypeSpellListNames
                            .computeIfAbsent(archetype.getArchetype(), s->new ReadOnlyStringWrapper())
                            .set(spells.getListName());
                } else {
                    spellListName.set(spells.getListName());
                }
            }
        }
        return builder;
    }

    AbilitySlot makeAbilitySlot(Element propElem, int level) {
        String abilityName = propElem.getAttribute("name");
        int slotLevel = level;
        if(!propElem.getAttribute("level").equals(""))
            slotLevel = Integer.parseInt(propElem.getAttribute("level").toLowerCase().trim());
        switch(propElem.getAttribute("state").toLowerCase().trim()){
            case "filled":
                NodeList ability = propElem.getElementsByTagName("Ability");
                if(ability.getLength() > 0) {
                    Element temp = (Element) ability.item(0);
                    return new FilledSlot(abilityName,
                            slotLevel, makeAbility(temp, abilityName, level).build());
                }else{
                    String type = propElem.getAttribute("type");
                    if(type.equals("")) type = "General";
                    Type dynamicType = getDynamicType(type);
                    DynamicFilledSlot contents = new DynamicFilledSlot(abilityName, slotLevel,
                            propElem.getAttribute("contents"),
                            dynamicType, dynamicType.equals(Type.Class));
                    if(dynamicType.equals(Type.Class))
                        dynSlots.add(contents);
                    return contents;
                }
            case "feat":
                return new FeatSlot(abilityName, slotLevel,
                        getTypes(propElem.getAttribute("type")));
            case "choice":
                return new SingleChoiceSlot(abilityName, slotLevel,
                        makeAbilities(propElem.getChildNodes()));
        }
        return null;
    }

    protected static Type getDynamicType(String type) {
        return Type.valueOf(type.trim().replaceAll(" [fF]eat", ""));
    }

    private static List<AbilityMod> getBoosts(int count, int level) {
        List<AbilityMod> boosts = new ArrayList<>(count);
        for(int i=0; i<count; i++)
            boosts.add(new AbilityModChoice(Type.get(level)));
        return boosts;
    }

    private static List<AttributeMod> addMods(String textContent, Proficiency prof) {
        List<AttributeMod> mods = new ArrayList<>();
        String[] split = textContent.split(",");
        for(String str: split) {
            if (!str.trim().equals("")) {
                String[] orCheck = str.trim().split(" or ");
                if(orCheck.length > 1) {
                    mods.add(new AttributeModSingleChoice(Attribute.robustValueOf(orCheck[0]),
                            Attribute.robustValueOf(orCheck[1]), prof));
                }else {
                    if (camelCaseWord(str.trim()).startsWith("Lore")) {
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

    public Ability.Builder makeAbility(Element curr) {
        String level = curr.getAttribute("level");
        return makeAbility(curr, curr.getAttribute("name"), (level.isBlank()) ? 0 : Integer.parseInt(level));
    }

    protected List<AbilityMod> getAbilityMods(String bonuses, String penalties, Type type) {
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

    protected AbilityMod getAbilityBonus(String abilityString, Type type) {
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
