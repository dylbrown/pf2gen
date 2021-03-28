package model.xml_parsers.equipment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.attributes.Attribute;
import model.attributes.AttributeBonus;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Trait;
import model.enums.Type;
import model.items.BaseItem;
import model.items.BaseItemChoices;
import model.items.Item;
import model.items.runes.ArmorRune;
import model.items.runes.WeaponRune;
import model.items.weapons.Damage;
import model.items.weapons.DamageType;
import model.items.weapons.Dice;
import model.player.ArbitraryChoice;
import model.spells.Spell;
import model.spells.Tradition;
import model.util.ObjectNotFoundException;
import model.util.StringUtils;
import model.xml_parsers.FileLoader;
import model.xml_parsers.SpellsLoader;
import model.xml_parsers.TraitsLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static model.util.StringUtils.camelCaseWord;

public class ItemLoader extends FileLoader<Item> {
    private static final ItemAbilityLoader abilityLoader = new ItemAbilityLoader(null, null, null);

    public ItemLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected String getName(Item item) {
        return item.getRawName();
    }

    @Override
    protected void loadMultiple(String category, String location) {
        if(!loadTracker.isNotLoaded(location))
            return;
        loadTracker.setLoaded(location);
        File subFile = getSubFile(location);
        Document doc = getDoc(subFile);
        Consumer<Element> parser = (curr) -> {
            Item item = parseItem(subFile, curr);
            addItem(item.getCategory(), item);
        };
        iterateElements(doc, "Item", parser);
        iterateElements(doc, "ArmorRune", parser);
        iterateElements(doc, "WeaponRune", parser);
    }

    @Override
    protected Item parseItem(File file, Element item) {
        String niceName = StringUtils.unclean(file.getName().replaceAll(".pfdyl", ""));
        switch(StringUtils.clean(item.getTagName())) {
            case "weaponrune": return makeWeaponRune(niceName, item);
            case "armorrune": return makeArmorRune(niceName, item);
        }
        return makeItem(niceName, item);
    }

    private Item makeItem(String niceName, Element item) {
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.setCategory(niceName);
        Node parentNode = item.getParentNode();
        if(parentNode instanceof Element && ((Element) parentNode).getTagName().equals("SubCategory")) {
            builder.setSubCategory(((Element) parentNode).getAttribute("name"));
        }
        if(item.hasAttribute("category"))
            builder.setCategory(item.getAttribute("category"));
        if(item.hasAttribute("level"))
            builder.setLevel(Integer.parseInt(item.getAttribute("level")));
        NodeList nodeList = item.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            parseTag(trim, curr, builder, this);
        }
        return builder.build();
    }


    private Item makeArmorRune(String niceName, Element item) {
        BaseItem.Builder builder = new BaseItem.Builder();
        ArmorRune.Builder runeExt = builder.getExtension(ArmorRune.Builder.class);
        builder.setCategory("Runes");
        Node parentNode = item.getParentNode();
        if(parentNode instanceof Element && ((Element) parentNode).getTagName().equals("SubCategory")) {
            builder.setSubCategory(((Element) parentNode).getAttribute("name"));
        }
        if(item.hasAttribute("level"))
            builder.setLevel(Integer.parseInt(item.getAttribute("level")));
        if(item.hasAttribute("fundamental"))
            runeExt.setFundamental(true);
        NodeList nodeList = item.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            if ("BonusAC".equals(curr.getTagName())) {
                runeExt.setBonusAC(Integer.parseInt(trim));
            }else if("GrantsProperties".equals(curr.getTagName())){
                runeExt.setGrantsProperties(Integer.parseInt(trim));
            } else {
                parseTag(trim, curr, builder, this);
            }
        }
        return builder.build();
    }

    private Item makeWeaponRune(String niceName, Element item) {
        BaseItem.Builder builder = new BaseItem.Builder();
        WeaponRune.Builder runeExt = builder.getExtension(WeaponRune.Builder.class);
        builder.setCategory("Runes");
        Node parentNode = item.getParentNode();
        if(parentNode instanceof Element && ((Element) parentNode).getTagName().equals("SubCategory")) {
            builder.setSubCategory(((Element) parentNode).getAttribute("name"));
        }
        if(item.hasAttribute("level"))
            builder.setLevel(Integer.parseInt(item.getAttribute("level")));
        if(item.hasAttribute("fundamental"))
            runeExt.setFundamental(true);
        NodeList nodeList = item.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName()) {
                case "AttackBonus":
                    runeExt.setAttackBonus(Integer.parseInt(trim));
                    break;
                case "BonusDamage":
                    runeExt.setBonusDamage(parseDamage(trim));
                    break;
                case "WeaponDice":
                    runeExt.setBonusWeaponDice(Integer.parseInt(trim));
                    break;
                case "GrantsProperties":
                    runeExt.setGrantsProperties(Integer.parseInt(trim));
                    break;
                default:
                    parseTag(trim, curr, builder, this);
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

    void parseTag(String trim, Element curr, BaseItem.Builder builder, FileLoader<?> loader) {
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
                if (trim.equalsIgnoreCase("L"))
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
                    Trait trait = null;
                    try {
                        trait = loader.findFromDependencies("Trait",
                                TraitsLoader.class,
                                item.trim());
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                    return trait;
                }).filter(Objects::nonNull)
                    .forEachOrdered(builder::addTrait);
                break;
            case "Bonuses":
                Arrays.stream(trim.split(",")).map((item)->{
                    String[] s = item.trim().split(" ");
                    if(s.length != 2) return null;
                    return new AttributeBonus(
                            Attribute.valueOf(s[1]),
                            Integer.parseInt(s[0]),
                            Type.Item);
                }).filter(Objects::nonNull)
                    .forEachOrdered(builder::addBonus);
                break;
            case "Ability":
                builder.addAbility(abilityLoader.makeAbility(curr).build());
                break;
            case "Choices":
                NodeList childNodes = curr.getChildNodes();
                for(int j = 0; j < childNodes.getLength(); j++) {
                    Node item = childNodes.item(j);
                    if(item.getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element childElem = (Element) item;
                    switch (StringUtils.clean(childElem.getTagName())) {
                        case "spell":
                            addSpellChoice(builder, childElem);
                            break;
                    }
                }
                break;
        }
    }

    private <T> void addSpellChoice(BaseItem.Builder builder, Element choice) {
        ArbitraryChoice.Builder<Spell> spell = new ArbitraryChoice.Builder<>();
        spell.setOptionsClass(Spell.class);
        spell.setMaxSelections(1);
        NodeList childNodes = choice.getChildNodes();
        Set<Integer> levels = new HashSet<>();
        Set<Tradition> traditions = new HashSet<>();
        for(int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if(item.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) item;
            switch (curr.getTagName().toLowerCase()) {
                case "name":
                    spell.setName(curr.getTextContent().trim());
                    break;
                case "levels":
                    for (String s : curr.getTextContent().split(", ?")) {
                        int dash = s.indexOf("-");
                        if(dash != -1) {
                            int start = Integer.parseInt(s.substring(0, dash));
                            int end = Integer.parseInt(s.substring(dash + 1));
                            for(int j = start; j <= end; j++) {
                                levels.add(j);
                            }
                        }else{
                            levels.add(Integer.parseInt(s));
                        }
                    }
                    break;
                case "traditions":
                    for (String s : curr.getTextContent().split(", ?")) {
                        traditions.add(Tradition.valueOf(StringUtils.camelCaseWord(s.trim())));
                    }
                    break;
            }
        }
        if(levels.isEmpty())
            levels.addAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        if(traditions.isEmpty())
            traditions.addAll(Arrays.asList(Tradition.values()));
        ObservableList<Spell> choices = FXCollections.observableArrayList();
        SpellsLoader loader = getSource().getLoader(SpellsLoader.class);
        if(loader != null) {
            for (Spell value : loader.getAll().values()) {
                if(value.getTraditions().stream().anyMatch(traditions::contains) &&
                levels.contains(value.getLevel())) { // TODO: Support heightened level
                    choices.add(value);
                }
            }
        }
        spell.setChoices(choices);
        builder.getExtension(BaseItemChoices.Builder.class).addChoice(spell.build());
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
