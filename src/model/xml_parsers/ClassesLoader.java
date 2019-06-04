package model.xml_parsers;

import model.abc.Class;
import model.abilities.Ability;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.ChoiceSlot;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.FilledSlot;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.enums.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

public class ClassesLoader extends FileLoader<Class> {

    private List<Class> classes;
    public ClassesLoader() {
        path = new File("data/classes");
    }

    @Override
    public List<Class> parse() {
        if(classes == null) {
            classes = new ArrayList<>();
            for (Document doc : getDocs(path)) {
                NodeList classProperties = doc.getElementsByTagName("class").item(0).getChildNodes();

                String name = ""; int hp = 0; int skillIncreases = 0; AbilityMod abilityMod = null; Map<Integer, List<AbilitySlot>> table = new HashMap<>(); List<Ability> feats = new ArrayList<>(); String description="";

                for(int i=0; i<classProperties.getLength(); i++) {
                    if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element curr = (Element) classProperties.item(i);
                    String trim = curr.getTextContent().trim();
                    switch(curr.getTagName()){
                        case "Name":
                            name = trim;
                            break;
                        case "Description":
                            description = trim;
                            break;
                        case "HP":
                            hp = Integer.parseInt(trim);
                            break;
                        case "SkillIncreases":
                            skillIncreases = Integer.parseInt(trim);
                            break;
                        case "AbilityChoices":
                            if(curr.getElementsByTagName("AbilityScore").getLength() == 1) {
                                abilityMod = new AbilityMod(AbilityScore.valueOf(camelCaseWord(curr.getElementsByTagName("AbilityScore").item(0).getTextContent())), true, Type.Class);
                            }else{
                                List<AbilityScore> scores = new ArrayList<>();
                                for(int j=0; j<curr.getElementsByTagName("AbilityScore").getLength(); j++) {
                                    scores.add(AbilityScore.valueOf(camelCaseWord(curr.getElementsByTagName("AbilityScore").item(j).getTextContent())));
                                }
                                abilityMod = new AbilityModChoice(scores, Type.Class);
                            }
                            break;
                        case "FeatureList":
                            int level = Integer.parseInt(curr.getAttribute("level"));
                            List<AbilitySlot> abilitySlots = new ArrayList<>();
                            for(int j=0; j<curr.getElementsByTagName("AbilitySlot").getLength(); j++) {
                                if(curr.getElementsByTagName("AbilitySlot").item(j).getNodeType() != Node.ELEMENT_NODE)
                                    continue;
                                Element slotNode = (Element) curr.getElementsByTagName("AbilitySlot").item(j);
                                String abilityName = slotNode.getAttribute("name");
                                switch(slotNode.getAttribute("state").toLowerCase().trim()){
                                    case "filled":
                                        Element temp = (Element) slotNode.getElementsByTagName("Ability").item(0);
                                        abilitySlots.add(new FilledSlot(abilityName, level, makeAbility(temp, abilityName, level)));
                                        break;
                                    case "feat":
                                        abilitySlots.add(new FeatSlot(abilityName, level, getTypes(slotNode.getAttribute("type"))));
                                        break;
                                    case "choice":
                                        abilitySlots.add(new ChoiceSlot(abilityName, level, makeAbilities(slotNode.getChildNodes())));
                                        break;
                                }
                            }
                            table.put(level, abilitySlots);
                            break;
                        case "Feats":
                            NodeList featNodes = curr.getChildNodes();
                            for (int j = 0; j < featNodes.getLength(); j++) {
                                if(featNodes.item(j) instanceof Element)
                                    feats.add(makeAbility((Element) featNodes.item(j), ((Element) featNodes.item(j)).getAttribute("name")));
                            }
                            break;
                    }
                }
                classes.add(new Class(name, description, hp, skillIncreases, abilityMod, table, feats));
            }
        }
        return Collections.unmodifiableList(classes);
    }
}
