package model.xml_parsers.abc;

import model.abc.PClass;
import model.ability_slots.*;
import model.data_managers.sources.SourceConstructor;
import model.enums.Type;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PClassesLoader extends ACLoader<PClass, PClass.Builder> {

    static{
        source = e -> Type.Class;
    }

    public PClassesLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }

    @Override
    void parseElement(Element curr, String trim, PClass.Builder builder) {
        switch(curr.getTagName()){
            case "SkillIncreases":
                builder.setSkillIncreases(Integer.parseInt(trim));
                break;
            case "AbilityChoices":
                builder.setAbilityMods(Collections.singletonList(getAbilityBonus(trim, Type.Class)));
                break;
            case "FeatureList":
                int level = Integer.parseInt(curr.getAttribute("level"));
                List<AbilitySlot> abilitySlots = new ArrayList<>();
                NodeList childNodes = curr.getChildNodes();
                for(int j = 0; j< childNodes.getLength(); j++) {
                    if(childNodes.item(j).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element slotNode = (Element) childNodes.item(j);
                    if(!slotNode.getTagName().equals("AbilitySlot")) continue;
                    String abilityName = slotNode.getAttribute("name");
                    switch(slotNode.getAttribute("state").toLowerCase().trim()){
                        case "filled":
                            NodeList ability = slotNode.getElementsByTagName("Ability");
                            if(ability.getLength() > 0) {
                                Element temp = (Element) ability.item(0);
                                abilitySlots.add(new FilledSlot(abilityName, level, makeAbility(temp, abilityName, level)));
                            }else{
                                String type = slotNode.getAttribute("type");
                                if(type.equals("")) type = "General";
                                DynamicFilledSlot dynamicFilledSlot = new DynamicFilledSlot(abilityName, level,
                                        slotNode.getAttribute("contents"),
                                        getDynamicType(type), true);
                                abilitySlots.add(dynamicFilledSlot);
                                dynSlots.add(dynamicFilledSlot);
                            }
                            break;
                        case "feat":
                            abilitySlots.add(new FeatSlot(abilityName, level, getTypes(slotNode.getAttribute("type"))));
                            break;
                        case "choice":
                            abilitySlots.add(new SingleChoiceSlot(abilityName, level, makeAbilities(slotNode.getChildNodes())));
                            break;
                    }
                }
                builder.addToTable(level, abilitySlots);
                break;
            default:
                super.parseElement(curr, trim, builder);
        }
    }

    @Override
    protected PClass parseItem(File file, Element item) {
        NodeList classProperties = item.getChildNodes();

        PClass.Builder builder = new PClass.Builder();
        dynSlots = new ArrayList<>();

        for(int i=0; i<classProperties.getLength(); i++) {
            if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) classProperties.item(i);
            parseElement(curr, curr.getTextContent().trim(), builder);
        }
        PClass pClass = builder.build();
        for (DynamicFilledSlot dynSlot : dynSlots) {
            dynSlot.setpClass(pClass);
        }
        return pClass;
    }
}
