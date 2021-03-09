package model.xml_parsers;

import model.abilities.Ability;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.creatures.*;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Language;
import model.enums.Trait;
import model.enums.Type;
import model.items.CustomTrait;
import model.items.Item;
import model.items.ItemInstance;
import model.items.armor.Armor;
import model.items.runes.runedItems.RunedArmor;
import model.items.runes.runedItems.RunedWeapon;
import model.items.runes.runedItems.Runes;
import model.items.weapons.Weapon;
import model.util.ObjectNotFoundException;
import model.xml_parsers.equipment.ArmorLoader;
import model.xml_parsers.equipment.ItemLoader;
import model.xml_parsers.equipment.WeaponsLoader;
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
        BaseCreature.Builder builder = new BaseCreature.Builder();
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
                    try {
                        builder.setFamily(findFromDependencies("CreatureFamily",
                                CreatureFamilyLoader.class,
                                contents));
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case "Traits":
                    builder.setTraits(
                            Arrays.stream(contents.split(", "))
                                    .map(s->{
                                        Trait trait = null;
                                        try {
                                            trait = findFromDependencies("Trait",
                                                    TraitsLoader.class,
                                                    s);
                                        } catch (ObjectNotFoundException e) {
                                            e.printStackTrace();
                                        }
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
                                Item equipment = null;
                                try {
                                    equipment = getItem(s, true, builder);
                                } catch (ObjectNotFoundException e) {
                                    System.out.println(e.getMessage());
                                }
                                if(equipment != null)
                                    builder.addItem(new CreatureItem(equipment));
                                else
                                    builder.addItem(new CreatureItem(s));
                            });
                    break;
                case "AC":
                    endMod = contents.indexOf(" (");
                    if(endMod != -1) {
                        int endModString = contents.indexOf(')', endMod);
                        builder.setACMods(contents.substring(endMod + 2, endModString));
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

    private Item getItem(String name, boolean canBeEnchanted, BaseCreature.Builder builder) throws ObjectNotFoundException {
        if(name.endsWith("armor"))
            return getItem(name.substring(0, name.length()-6), canBeEnchanted, builder);
        if(name.matches(".*\\([^+][^)]*\\) *\\z")) {
            int bracket = name.lastIndexOf('(');
            String newName = name.substring(0, bracket);
            String ammo = name.substring(bracket + 1, name.indexOf(')', bracket));
            name = newName;
            if(builder != null && ammo.matches("\\A\\d+ .*")) {
                try {
                    Item ammoItem = getItem(ammo, false, null);
                    builder.addItem(new CreatureItem(ammoItem));
                } catch (ObjectNotFoundException e) {
                    builder.addItem(new CreatureItem(ammo));
                }
            }
        }
        try {
            return findFromDependencies("Equipment",
                    ItemLoader.class,
                    name);
        } catch (ObjectNotFoundException e) {
            try {
                return findFromDependencies("Armor",
                        ArmorLoader.class,
                        name);
            } catch (ObjectNotFoundException e2) {
                try {
                    return findFromDependencies("Weapon",
                            WeaponsLoader.class,
                            name);
                } catch (ObjectNotFoundException e3) {
                    if(canBeEnchanted) {
                        try {
                            return parseEnchantedItem(name, builder);
                        } catch (ObjectNotFoundException e4) {
                            throw e;
                        }
                    } else throw e;
                }
            }
        }
    }

    private enum ItemType {
        Armor, Weapon
    }

    private Item parseEnchantedItem(String s, BaseCreature.Builder builder) throws ObjectNotFoundException {
        String[] words = s.split(" ");
        Item baseItem = null;
        ItemType type = null;
        int startOfItem = words.length - 1;
        while(baseItem == null && startOfItem > 0) {
            if(Arrays.asList("composite", "half", "hand", "bo").contains(words[startOfItem - 1])) {
                startOfItem--;
                continue;
            }
            String itemName = String.join(" ", Arrays.asList(words).subList(startOfItem, words.length));
            try{
                baseItem = getItem(itemName, true, builder);
            }catch (ObjectNotFoundException e) {
                startOfItem--;
                continue;
            }
            if(!baseItem.hasExtension(Weapon.class) && !baseItem.hasExtension(Armor.class)) {
                baseItem = null;
                startOfItem--;
            }else if(baseItem.hasExtension(Armor.class))
                type = ItemType.Armor;
            else
                type = ItemType.Weapon;
        }
        if(baseItem == null) {
            throw new ObjectNotFoundException(s, "Item");
        }
        ItemInstance runedItem = new ItemInstance(baseItem);
        Runes<?> runes;
        if(type == ItemType.Armor){
            runedItem.addExtension(RunedArmor.class);
            runes = runedItem.getExtension(RunedArmor.class).getRunes();
        }else{
            runedItem.addExtension(RunedWeapon.class);
            runes = runedItem.getExtension(RunedWeapon.class).getRunes();
        }
        for(int i = 0; i < startOfItem; i++) {
            String word = words[i].toLowerCase();
            switch (word) {
                case "greater":
                    word = words[i+1].toLowerCase() + " (Greater)";
                    break;
                case "major":
                    word = words[i+1].toLowerCase() + " (Major)";
                    break;
                case "+1":
                    if(baseItem.hasExtension(Weapon.class))
                        word = "Weapon Potency (+1)";
                    else
                        word = "Armor Potency (+1)";
                    break;
                case "+2":
                    if(baseItem.hasExtension(Weapon.class))
                        word = "Weapon Potency (+2)";
                    else
                        word = "Armor Potency (+2)";
                    break;
                case "+3":
                    if(baseItem.hasExtension(Weapon.class))
                        word = "Weapon Potency (+3)";
                    else
                        word = "Armor Potency (+3)";
                    break;
            }
            runes.tryToAddRune(getItem(word, false, null));
        }
        return runedItem;
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
                    try {
                        list.addSpell(findFromDependencies("Spell",
                                SpellsLoader.class,
                                s), levelNumber);
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    int endBracket = s.indexOf(')');
                    try {
                        list.addSpell(findFromDependencies("Spell",
                                SpellsLoader.class,
                                s.substring(0, bracket - 1)),
                                levelNumber,
                                s.substring(bracket + 1, endBracket));
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
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
                        item = item.trim();
                        String[] s = item.split(" ", 2);
                        if(s.length == 1) {
                            Trait trait = null;
                            try {
                                trait = findFromDependencies("Trait",
                                        TraitsLoader.class,
                                        s[0]);
                            } catch (ObjectNotFoundException e) {
                                e.printStackTrace();
                            }
                            return trait;
                        }  else {
                            String custom = s[1];
                            Trait trait = null;
                            try {
                                trait = findFromDependencies("Trait",
                                        TraitsLoader.class,
                                        s[0]);
                            } catch (ObjectNotFoundException e) {
                                int space = item.indexOf(' ');
                                space = item.indexOf(' ', space + 1);
                                if(space == -1) {
                                    custom = "";
                                    try {
                                        trait = findFromDependencies("Trait",
                                                TraitsLoader.class,
                                                item);
                                    } catch (ObjectNotFoundException objectNotFoundException) {
                                        System.out.println(e.getMessage());
                                    }
                                }else {
                                    custom = item.trim().substring(space+1);
                                    try {
                                        trait = findFromDependencies("Trait",
                                                TraitsLoader.class,
                                                item.trim().substring(0, space));
                                    } catch (ObjectNotFoundException e2) {
                                        e2.printStackTrace();
                                    }
                                }
                            }
                            if(custom.isBlank())
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
