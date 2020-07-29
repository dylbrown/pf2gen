package model.xml_parsers.equipment;

import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.ArmorProficiency;
import model.equipment.CustomTrait;
import model.equipment.armor.Armor;
import model.equipment.armor.ArmorGroup;
import model.equipment.armor.Shield;
import model.xml_parsers.FileLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static model.util.StringUtils.camelCase;

public class ArmorLoader extends FileLoader<Armor> {

    private final Map<String, ArmorGroup> armorGroups = new HashMap<>();
    private final Map<String, CustomTrait> armorTraits = new HashMap<>();

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

        iterateElements(doc, "ArmorTrait", (curr)->{
            String name = curr.getElementsByTagName("Name").item(0).getTextContent().trim();
            String desc = curr.getElementsByTagName("Description").item(0).getTextContent().trim();
            armorTraits.put(name, new CustomTrait(name, desc));
        });
        iterateElements(doc, "Armor", (curr)->{
            Armor armor = getArmor(curr);
            addItem(armor.getSubCategory(), armor);
        });
    }

    @Override
    protected Armor parseItem(File file, Element item) {
        return getArmor(item);
    }

    private Armor getArmor(Element armor) {
        Armor.Builder builder = new Armor.Builder();
        Shield.Builder shieldBuilder = null;
        Node proficiencyNode= armor.getParentNode();
        if(proficiencyNode.getNodeName().trim().equals("Shield"))
            builder = shieldBuilder = new Shield.Builder(builder);
        builder.setProficiency(ArmorProficiency.valueOf(camelCase(proficiencyNode.getNodeName())));
        NodeList nodeList = armor.getChildNodes();

        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName()) {
                case "AC":
                    builder.setAC(Integer.parseInt(trim.replaceAll("\\+","")));
                    break;
                case "MaxDex":
                    builder.setMaxDex(Integer.parseInt(trim.replaceAll("\\+","")));
                    break;
                case "ACP":
                    builder.setACP(Integer.parseInt(trim.replaceAll("\\+","")));
                    break;
                case "SpeedPenalty":
                    builder.setSpeedPenalty(Integer.parseInt(trim.replaceAll("\\+","")));
                    break;
                case "Group":
                    builder.setGroup(armorGroups.get(trim.toLowerCase()));
                    break;
                case "Strength":
                    builder.setStrength(Integer.parseInt(trim));
                    break;
                case "Hardness":
                    if(shieldBuilder == null)
                        builder = shieldBuilder = new Shield.Builder(builder);
                    shieldBuilder.setHardness(Integer.parseInt(trim));
                    break;
                case "HP":
                    if(shieldBuilder == null)
                        builder = shieldBuilder = new Shield.Builder(builder);
                    shieldBuilder.setHP(Integer.parseInt(trim));
                    break;
                case "BT":
                    if(shieldBuilder == null)
                        builder = shieldBuilder = new Shield.Builder(builder);
                    shieldBuilder.setBT(Integer.parseInt(trim));
                    break;
                case "Traits":
                    Arrays.stream(trim.split(",")).map((item)->armorTraits.get(camelCase(item.trim().split(" ")[0]))).forEachOrdered(builder::addArmorTrait);
                    break;
                default:
                    EquipmentLoader.parseTag(trim, curr, builder);
            }
        }
        return (shieldBuilder != null) ? shieldBuilder.build() : builder.build();
    }
}
