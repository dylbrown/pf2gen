package model.xml_parsers;

import model.enums.ArmorProficiency;
import model.equipment.*;
import model.equipment.armor.Armor;
import model.equipment.armor.ArmorGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static model.util.StringUtils.camelCase;
//TODO: Implement Builder Pattern
public class ArmorLoader extends FileLoader<Armor> {

    private List<Armor> armorAndShields;
    private List<Equipment> equipment;

    private final Map<String, ArmorGroup> armorGroups = new HashMap<>();
    private final Map<String, CustomTrait> armorTraits = new HashMap<>();

    public ArmorLoader() {
        path = new File("data/equipment/armor_and_shields.pfdyl");
    }
    @Override
    public List<Armor> parse() {
        if(armorAndShields == null) {
            armorAndShields = new ArrayList<>();
            Document doc = getDoc(path);

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

            iterateElements(doc, "Armor", (curr)-> armorAndShields.add(getArmor(curr)));
        }
        return Collections.unmodifiableList(armorAndShields);
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
                    ItemLoader.parseTag(trim, curr, builder);
            }
        }
        return (shieldBuilder != null) ? shieldBuilder.build() : builder.build();
    }

    public List<Equipment> getEquipmentList() {
        if(equipment == null) equipment = parse().stream().map(a->(Equipment) a).collect(Collectors.toList());
        return equipment;
    }
}
