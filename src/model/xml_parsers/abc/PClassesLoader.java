package model.xml_parsers.abc;

import model.abc.PClass;
import model.abilities.Ability;
import model.ability_slots.AbilitySlot;
import model.ability_slots.DynamicFilledSlot;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Type;
import model.util.Pair;
import model.xml_parsers.AbilityLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PClassesLoader extends ACLoader<PClass, PClass.Builder> {
    private final AbilityLoader<Ability> abilityLoader;

    static{
        sources.put(PClassesLoader.class, e -> Type.Class);
    }

    public PClassesLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
        abilityLoader = new AbilityLoader<>(null, root, sourceBuilder) {
            @Override
            protected Ability parseItem(File file, Element item) {
                throw new UnsupportedOperationException();
            }
        };
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
                    abilitySlots.add(abilityLoader.makeAbilitySlot(slotNode, level));
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

        for(int i=0; i<classProperties.getLength(); i++) {
            if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) classProperties.item(i);
            parseElement(curr, curr.getTextContent().trim(), builder);
        }
        PClass pClass = builder.build();
        while (!dynSlots.isEmpty()) {
            Pair<String, DynamicFilledSlot> pair = dynSlots.pop();
            if(pair.first.equalsIgnoreCase(pClass.getName()) || pair.first.isBlank())
                pair.second.setPClass(pClass);
        }
        return pClass;
    }
}
