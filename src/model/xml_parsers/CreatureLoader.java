package model.xml_parsers;

import model.abilities.Ability;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.creatures.*;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.data_managers.sources.SourcesLoader;
import model.enums.Language;
import model.enums.Trait;
import model.enums.Type;
import model.equipment.CustomTrait;
import model.equipment.Equipment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CreatureLoader extends AbilityLoader<Creature> {
    public CreatureLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected Creature parseItem(File file, Element item) {
        Creature.Builder builder = new Creature.Builder();
        NodeList childNodes = item.getChildNodes();
        builder.setLevel(Integer.parseInt(item.getAttribute("level")));
        builder.setPage(Integer.parseInt(item.getAttribute("page")));
        for(int i = 0; i < childNodes.getLength(); i++) {
            if(childNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) childNodes.item(i);
            String contents = curr.getTextContent();
            switch (curr.getTagName()) {
                case "Name":
                    builder.setName(contents);
                    break;
                case "Family":
                    // TODO: Support Monster Families
                    break;
                case "Traits":
                    builder.setTraits(
                            Arrays.stream(contents.split(", "))
                                    .map(s->{
                                        Trait trait = Trait.valueOf(s);
                                        if(trait == null)
                                            System.out.println(s);
                                        return trait;
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList())
                    );
                    break;
                case "Perception":
                    int endMod = contents.indexOf(';');
                    if(endMod != -1) {
                        builder.setSenses(contents.substring(endMod + 2));
                    } else endMod = contents.length();
                    builder.setModifier(Attribute.Perception, Integer.parseInt(contents.substring(0, endMod)));
                    break;
                case "Languages":
                    int languagesEnd = contents.indexOf(';');
                    if(languagesEnd == -1) {
                        languagesEnd = contents.length();
                    } else {
                        builder.setSpecialLanguages(contents.substring(languagesEnd + 2));
                    }
                    builder.setLanguages(
                            Arrays.stream(contents.substring(0, languagesEnd).split(", "))
                                    .map(Language::testValueOf)
                                    .collect(Collectors.toList())
                    );
                    break;
                case "Skills":
                    String[] skills = contents.split(", ");
                    for(int j = 0; j < skills.length; j++) {
                        String s = skills[j];
                        int lastSpace = s.lastIndexOf(" ", s.indexOf('+'));
                        Attribute attribute = Attribute.robustValueOf(s.substring(0, lastSpace));
                        int nextSpace = s.indexOf(' ', lastSpace + 1);
                        builder.setModifier(attribute, Integer.parseInt(s.substring(lastSpace + 1,
                                (nextSpace == -1) ? s.length() : nextSpace))
                        );
                        if(nextSpace != -1) {
                            int endOfBrackets = s.indexOf(')', nextSpace + 2);
                            StringBuilder sBuilder = new StringBuilder(s);
                            while (endOfBrackets == -1) {
                                sBuilder.append(skills[++j]);
                                endOfBrackets = sBuilder.toString().indexOf(')', nextSpace + 2);
                            }
                            builder.addModifierSpecialInfo(
                                    attribute,
                                    sBuilder.substring(nextSpace + 2, endOfBrackets)
                            );
                        }
                    }
                    break;
                case "AbilityScores":
                    Arrays.stream(contents.split(", "))
                            .forEach(s->{
                                String[] split = s.split(" ");
                                builder.setModifier(AbilityScore.valueOf(split[0]), Integer.parseInt(split[1]));
                            });
                    break;
                case "Items":
                    Arrays.stream(contents.split(", "))
                            .forEach(s->{
                                Equipment equipment = SourcesLoader.instance().equipment().find(s);
                                if(equipment != null)
                                    builder.addItem(new CreatureItem(equipment));
                                else
                                    builder.addItem(new CreatureItem(s));
                            });
                    break;
                case "AC":
                    endMod = contents.indexOf(" (");
                    if(endMod != -1) {
                        builder.setACMods(contents.substring(endMod + 2));
                    } else endMod = contents.length();
                    builder.setAC(Integer.parseInt(contents.substring(0, endMod)));
                    break;
                case "Saves":
                    endMod = contents.indexOf(';');
                    if(endMod != -1) {
                        builder.setSaveMods(contents.substring(endMod + 2));
                    } else endMod = contents.length();
                    for (String s : contents.substring(0, endMod).split(", ")) {
                        String[] split = s.split(" ");
                        builder.setModifier(Attribute.robustValueOf(split[0]), Integer.parseInt(split[1]));
                    }
                    break;
                case "HP":
                    endMod = contents.indexOf(' ');
                    if(endMod != -1) {
                        builder.setHealthMods(contents.substring(endMod + 2));
                    } else endMod = contents.length();
                    builder.setHP(Integer.parseInt(contents.substring(0, endMod).replaceAll(",", "")));
                    break;
                case "Immunities":
                    builder.setImmunities(contents);
                    break;
                case "Resistances":
                    builder.setResistances(contents);
                    break;
                case "Weaknesses":
                    builder.setWeaknesses(contents);
                    break;
                case "Speed":
                    builder.setSpeed(contents);
                    break;
                case "Description":
                    builder.setDescription(contents);
                    break;
                case "MiscAbilities":
                case "DefensiveAbilities":
                case "OffensiveAbilities":
                    List<Ability> abilities = new ArrayList<>();
                    NodeList abilityNodes = curr.getChildNodes();
                    for(int j = 0; j < abilityNodes.getLength(); j++) {
                        if(abilityNodes.item(j).getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element abilityElem = (Element) abilityNodes.item(j);
                        Ability.Builder ability = makeAbility(abilityElem);
                        switch (curr.getTagName()) {
                            case "MiscAbilities":
                                ability.setType(Type.Misc);
                                break;
                            case "DefensiveAbilities":
                                ability.setType(Type.Defensive);
                                break;
                            case "OffensiveAbilities":
                                ability.setType(Type.Offensive);
                                break;
                        }
                        abilities.add(ability.build());
                    }
                    switch (curr.getTagName()) {
                        case "MiscAbilities":
                            builder.setMiscAbilities(abilities);
                            break;
                        case "DefensiveAbilities":
                            builder.setDefensiveAbilities(abilities);
                            break;
                        case "OffensiveAbilities":
                            builder.setOffensiveAbilities(abilities);
                            break;
                    }
                    break;
                case "Attacks":
                    List<Attack> attacks = new ArrayList<>();
                    NodeList attackNodes = curr.getChildNodes();
                    for(int j = 0; j < attackNodes.getLength(); j++) {
                        if(attackNodes.item(j).getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element attackElem = (Element) attackNodes.item(j);
                        attacks.add(makeAttack(attackElem));
                    }
                    builder.setAttacks(attacks);
                    break;
                case "Spells":
                    String title = curr.getElementsByTagName("Title").item(0).getTextContent();
                    CreatureSpellList list = new CreatureSpellList(title);
                    NodeList spellNodes = curr.getChildNodes();
                    for(int j = 0; j < spellNodes.getLength(); j++) {
                        if(spellNodes.item(j).getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element spellElem = (Element) spellNodes.item(j);
                        String subContents = spellElem.getTextContent();
                        switch(spellElem.getTagName()) {
                            case "DC":
                                list.setDC(Integer.parseInt(subContents));
                                break;
                            case "Attack":
                                list.setAttack(Integer.parseInt(subContents));
                                break;
                            case "Spells":
                                makeSpells(list, spellElem);
                                break;
                        }
                    }
                    break;
            }
        }
        return builder.build();
    }

    private void makeSpells(CreatureSpellList list, Element elem) {
        String level = elem.getAttribute("level");
        int space = level.indexOf(" ");
        int levelNumber;
        if(level.startsWith("Constant")) levelNumber = -1;
        else levelNumber = (space == -1) ? Integer.parseInt(level) : Integer.parseInt(level.substring(0, space));
        if(space != -1 && levelNumber <= 0)
            list.setHeightenedLevel(Integer.parseInt(level.substring(space + 1).replaceAll("[^\\d]", "")));
        String slots = elem.getAttribute("slots");
        if(!slots.isBlank()) {
            list.addSlots(levelNumber, Integer.parseInt(slots));
            for (String s : elem.getTextContent().split(", ")) {
                int bracket = s.indexOf('(');
                if(bracket == -1) {
                    list.addSpell(SourcesLoader.instance().spells().find(s), levelNumber);
                } else {
                    int endBracket = s.indexOf(')');
                    list.addSpell(SourcesLoader.instance().spells().find(s.substring(0, bracket - 1)),
                            levelNumber,
                            s.substring(bracket + 1, endBracket));
                }
            }
        }
    }

    private Attack makeAttack(Element element) {
        Attack.Builder builder = new Attack.Builder();
        NodeList childNodes = element.getChildNodes();
        builder.setAttackType(AttackType.valueOf(element.getAttribute("type")));
        for(int i = 0; i < childNodes.getLength(); i++) {
            if(childNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element child = (Element) childNodes.item(i);
            String content = child.getTextContent();
            switch (child.getTagName()) {
                case "Name":
                    builder.setName(content);
                    break;
                case "AttackModifier":
                    builder.setModifier(Integer.parseInt(content));
                    break;
                case "Traits":
                    builder.setTraits(Arrays.stream(content.split(",")).map((item)->{
                        String[] s = item.trim().split(" ", 2);
                        if(s.length == 1) {
                            Trait trait = SourcesLoader.instance().traits().find(s[0]);
                            if(trait == null) {
                                System.out.println(item.trim());
                            }
                            return trait;
                        }  else {
                            String custom = s[1];
                            Trait trait = SourcesLoader.instance().traits()
                                    .find(s[0]);
                            if(trait == null) {
                                int space = item.trim().indexOf(' ');
                                space = item.trim().indexOf(' ', space + 1);
                                if(space == -1) {
                                    space = item.trim().length();
                                    custom = "";
                                }else custom = item.trim().substring(space+1);
                                trait = SourcesLoader.instance().traits()
                                        .find(item.trim().substring(0, space));
                            }
                            if(trait == null) {
                                System.out.println(item.trim());
                            }
                            if(custom.length() == 0)
                                return trait;
                            return new CustomTrait(trait, custom);
                        }
                    }).collect(Collectors.toList()));
                    break;
                case "Damage":
                    builder.setDamage(content);
                    break;
            }
        }
        return builder.build();
    }
}
