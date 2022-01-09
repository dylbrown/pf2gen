package model.xml_parsers.equipment;

import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.ArmorProficiency;
import model.enums.Trait;
import model.items.BaseItem;
import model.items.Item;
import model.items.armor.Armor;
import model.items.armor.ArmorGroup;
import model.items.armor.Shield;
import model.util.ObjectNotFoundException;
import model.xml_parsers.TraitsLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static model.util.StringUtils.capitalize;

public class ArmorLoader extends ItemLoader {

    private final Map<String, ArmorGroup> armorGroups = new HashMap<>();

    public ArmorLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected void loadMultiple(String category, String location) {

        loadTracker.setLoaded(location);
        File subFile = getSubFile(location);
        Document doc = getDoc(subFile);
        iterateElements(doc, "ArmorGroup", (curr)->{
            String name = curr.getElementsByTagName("Name").item(0).getTextContent().trim();
            String effect = curr.getElementsByTagName("Effect").item(0).getTextContent().trim();
            armorGroups.put(name.toLowerCase(), new ArmorGroup(effect, name));
        });

        iterateElements(doc, "Armor", (curr)->{
            Item armor = getArmor(curr);
            addItem(armor.getSubCategory(), armor);
        });
    }

    @Override
    protected Item parseItem(File file, Element item) {
        return getArmor(item);
    }

    private Item getArmor(Element armor) {
        BaseItem.Builder builder = new BaseItem.Builder(getSource());
        if(armor.getTagName().equalsIgnoreCase("shield"))
            builder.setCategory("Shields");
        else
            builder.setCategory("Armor");
        Armor.Builder armorExt = builder.getExtension(Armor.Builder.class);
        Node proficiencyNode= armor.getParentNode();
        ArmorProficiency armorProficiency = ArmorProficiency.valueOf(capitalize(proficiencyNode.getNodeName()));
        armorExt.setProficiency(armorProficiency);
        builder.setSubCategory(armorProficiency.name());
        NodeList nodeList = armor.getChildNodes();

        if(armor.hasAttribute("category"))
            builder.setCategory(armor.getAttribute("category"));

        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName()) {
                case "AC":
                    armorExt.setAC(Integer.parseInt(trim.replaceAll("\\+","")));
                    break;
                case "MaxDex":
                    armorExt.setMaxDex(Integer.parseInt(trim.replaceAll("\\+","")));
                    break;
                case "ACP":
                    armorExt.setACP(Integer.parseInt(trim.replaceAll("\\+","")));
                    break;
                case "SpeedPenalty":
                    armorExt.setSpeedPenalty(Integer.parseInt(trim.replaceAll("\\+","")));
                    break;
                case "Group":
                    armorExt.setGroup(armorGroups.get(trim.toLowerCase()));
                    break;
                case "Strength":
                    armorExt.setStrength(Integer.parseInt(trim));
                    break;
                case "Hardness":
                    builder.getExtension(Shield.Builder.class)
                            .setHardness(Integer.parseInt(trim));
                    break;
                case "HP":
                    builder.getExtension(Shield.Builder.class)
                            .setHP(Integer.parseInt(trim));
                    break;
                case "BT":
                    builder.getExtension(Shield.Builder.class)
                            .setBT(Integer.parseInt(trim));
                    break;
                case "Traits":
                    Arrays.stream(trim.split(",")).map((item)->{
                        Trait trait = null;
                        try {
                            trait = findFromDependencies("Trait",
                                    TraitsLoader.class,
                                    item.trim().split(" ")[0]);
                        } catch (ObjectNotFoundException e) {
                            e.printStackTrace();
                        }
                        return trait;
                    }).filter(Objects::nonNull).forEachOrdered(builder::addTrait);
                    break;
                default:
                    parseTag(trim, curr, builder, this);
            }
        }
        return builder.build();
    }
}
