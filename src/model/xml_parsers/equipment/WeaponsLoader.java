package model.xml_parsers.equipment;

import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Rarity;
import model.enums.Trait;
import model.enums.WeaponProficiency;
import model.items.BaseItem;
import model.items.CustomTrait;
import model.items.Item;
import model.items.weapons.*;
import model.util.ObjectNotFoundException;
import model.xml_parsers.FileLoader;
import model.xml_parsers.TraitsLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

import static model.util.StringUtils.capitalize;

public class WeaponsLoader extends ItemLoader {

    private static final Map<String, WeaponGroup> weaponGroups = new HashMap<>();

    public WeaponsLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected void loadMultiple(String category, String location) {
        loadTracker.setLoaded(location);
        File subFile = getSubFile(location);
        Document doc = getDoc(subFile);
        iterateElements(doc, "WeaponGroup", (curr)->{
            String name = curr.getElementsByTagName("Name").item(0).getTextContent().trim();
            String critEffect = curr.getElementsByTagName("CritEffect").item(0).getTextContent().trim();
            weaponGroups.put(name.toLowerCase(), new WeaponGroup(critEffect, name));
        });
        iterateElements(doc, "Weapon", (curr)->{
            Item weapon = getWeapon(curr, this);
            addItem(weapon.getExtension(Weapon.class).getProficiency().name(), weapon);
        });
    }

    @Override
    protected Item parseItem(File file, Element item) {
        return getWeapon(item, this);
    }

    public Item getWeapon(Element weapon, FileLoader<?> loader) {
        BaseItem.Builder item = new BaseItem.Builder();
        Weapon.Builder weaponExt = item.getExtension(Weapon.Builder.class);
        Node proficiencyNode= weapon.getParentNode();
        Node rangeNode = proficiencyNode.getParentNode();
        if(rangeNode.getNodeName().equals("Ranged")) {
            item.getExtension(RangedWeapon.Builder.class);
            item.setCategory("Ranged Weapons");
        }else item.setCategory("Weapons");


        if(weapon.hasAttribute("Uncommon") || weapon.hasAttribute("uncommon"))
            item.setRarity(Rarity.Uncommon);


        if(weapon.hasAttribute("category"))
            item.setCategory(weapon.getAttribute("category"));

        weaponExt.setProficiency(WeaponProficiency.valueOf(capitalize(proficiencyNode.getNodeName())));
        NodeList nodeList = weapon.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName()) {
                case "Damage":
                    String[] split = trim.split(" ");
                    String[] diceSplit = split[0].split("d");
                    weaponExt.setDamageDice(Dice.get(Integer.parseInt(diceSplit[0]), Integer.parseInt(diceSplit[1])));
                    switch(split[1].toUpperCase()) {
                        case "B":
                            weaponExt.setDamageType(DamageType.Bludgeoning);
                            break;
                        case "P":
                            weaponExt.setDamageType(DamageType.Piercing);
                            break;
                        case "S":
                            weaponExt.setDamageType(DamageType.Slashing);
                            break;
                    }
                    break;
                case "Range":
                    item.getExtension(RangedWeapon.Builder.class)
                            .setRange(Integer.parseInt(trim.split(" ")[0]));
                    break;
                case "Hands":
                    item.setHands(Integer.parseInt(trim));
                    break;
                case "Reload":
                    item.getExtension(RangedWeapon.Builder.class)
                            .setReload(Integer.parseInt(trim));
                    break;
                case "Bulk":
                    if (trim.toUpperCase().equals("L"))
                        item.setWeight(.1);
                    else
                        item.setWeight(Double.parseDouble(trim));
                    break;
                case "Group":
                    weaponExt.setGroup(weaponGroups.get(trim.toLowerCase()));
                    break;
                case "Traits":
                    Arrays.stream(trim.split(",")).map((traitString)->{
                        String[] s = traitString.trim().split(" ", 2);
                        Trait trait = null;
                        if(s.length == 1) {
                            try {
                                trait = loader.findFromDependencies("Trait",
                                        TraitsLoader.class,
                                        traitString.trim());
                            } catch (ObjectNotFoundException e) {
                                e.printStackTrace();
                            }
                            return trait;
                        }else {
                            try {
                                trait = loader.findFromDependencies("Trait",
                                        TraitsLoader.class,
                                        s[0]);
                                return new CustomTrait(trait, s[1]);
                            } catch (ObjectNotFoundException e) {
                                try {
                                    trait = loader.findFromDependencies("Trait",
                                            TraitsLoader.class,
                                            traitString);
                                    return trait;
                                } catch (ObjectNotFoundException e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                        return null;
                    }).filter(Objects::nonNull).forEachOrdered(item::addTrait);
                    break;
                default:
                    parseTag(trim, curr, item, loader);
                    break;
            }
        }
        return item.build();
    }

    public Map<String, WeaponGroup> getWeaponsGroups() {
        getAll();
        return Collections.unmodifiableMap(weaponGroups);
    }
}
