package model.xml_parsers;

import model.enums.ArmorProficiency;
import model.enums.Rarity;
import model.enums.Type;
import model.equipment.Armor;
import model.equipment.ArmorGroup;
import model.equipment.ItemTrait;
import model.equipment.Shield;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

import static model.enums.Type.None;
import static model.util.StringUtils.camelCase;
//TODO: Implement Builder Pattern
public class ArmorLoader extends FileLoader<Armor> {

    private List<Armor> armorAndShields;

    private final Map<String, ArmorGroup> armorGroups = new HashMap<>();
    private final Map<String, ItemTrait> armorTraits = new HashMap<>();

    public ArmorLoader() {
        path = new File("data/equipment/armor_and_shields.pfdyl");
    }
    @Override
    public List<Armor> parse() {
        if(armorAndShields == null) {
            armorAndShields = new ArrayList<>();
            Document doc = getDoc(path);
            NodeList groupNodes = doc.getElementsByTagName("ArmorGroup");
            for(int i=0; i<groupNodes.getLength(); i++) {
                if(groupNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) groupNodes.item(i);
                String name = curr.getElementsByTagName("Name").item(0).getTextContent().trim();
                String effect = curr.getElementsByTagName("Effect").item(0).getTextContent().trim();
                armorGroups.put(name.toLowerCase(), new ArmorGroup(effect, name));
            }

            NodeList traitNodes = doc.getElementsByTagName("ArmorTrait");
            for(int i=0; i<traitNodes.getLength(); i++) {
                if(traitNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) traitNodes.item(i);
                String name = curr.getElementsByTagName("Name").item(0).getTextContent().trim();
                String desc = curr.getElementsByTagName("Description").item(0).getTextContent().trim();
                armorTraits.put(name, new ItemTrait(name, desc));
            }

            NodeList armorNodes = doc.getElementsByTagName("Armor");
            for(int i=0; i<armorNodes.getLength(); i++) {
                if(armorNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) armorNodes.item(i);
                armorAndShields.add(getArmor(curr));
            }
        }
        return Collections.unmodifiableList(armorAndShields);
    }

    @Override
    protected Type getSource() {
        return None;
    }

    private Armor getArmor(Element armor) {
        double weight=0; double value=0; String name=""; String description = ""; Rarity rarity=Rarity.Common; List<ItemTrait> traits = new ArrayList<>(); boolean isShield=false; int acMod=0; int maxDex=0; int acp=0; int speedPenalty=0; int strength=0; ArmorGroup group = ArmorGroup.None; ArmorProficiency proficiency;
        int hardness=0;int hp=0; int bt=0;
        Node proficiencyNode= armor.getParentNode();
        if(proficiencyNode.getNodeName().trim().equals("Shield"))
            isShield = true;
        proficiency = ArmorProficiency.valueOf(camelCase(proficiencyNode.getNodeName()));
        NodeList nodeList = armor.getChildNodes();

        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName()) {
                case "Name":
                    name = trim;
                    break;
                case "Description":
                    description = trim;
                    break;
                case "AC":
                    acMod = Integer.parseInt(trim.replaceAll("\\+",""));
                    break;
                case "MaxDex":
                    maxDex = Integer.parseInt(trim.replaceAll("\\+",""));
                    break;
                case "ACP":
                    acp = Integer.parseInt(trim.replaceAll("\\+",""));
                    break;
                case "SpeedPenalty":
                    speedPenalty = Integer.parseInt(trim.replaceAll("\\+",""));
                    break;
                case "Price":
                    String[] split = trim.split(" ");
                    value = Double.parseDouble(split[0]);
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
                    break;
                case "Bulk":
                    if (trim.toUpperCase().equals("L"))
                        weight = .1;
                    else
                        weight = Double.parseDouble(trim);
                    break;
                case "Group":
                    group = armorGroups.get(trim.toLowerCase());
                    break;
                case "Strength":
                    strength = Integer.parseInt(trim);
                    break;
                case "Hardness":
                    hardness = Integer.parseInt(trim);
                    break;
                case "HP":
                    hp = Integer.parseInt(trim);
                    break;
                case "BT":
                    bt = Integer.parseInt(trim);
                    break;
                case "Traits":
                    Arrays.stream(trim.split(",")).map((item)->armorTraits.get(camelCase(item.trim().split(" ")[0]))).forEachOrdered(traits::add);
                    break;
            }
        }
        if(isShield)
            return new Shield(weight, value, name, description, rarity, acMod, maxDex, speedPenalty, hardness, hp, bt, traits);
        else
            return new Armor(weight, value, name, description, rarity, acMod, maxDex, acp, speedPenalty, strength, group, traits, proficiency);
    }
}
