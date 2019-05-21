package model.xml_parsers;

import model.abc.Ancestry;
import model.abilities.Ability;
import model.enums.Language;
import model.enums.Size;
import model.enums.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

public class AncestriesLoader extends FileLoader<Ancestry> {

    public AncestriesLoader() {
        path = new File("data/ancestries");
    }
    private List<Ancestry> ancestries;

    @Override
    public List<Ancestry> parse() {
        if(ancestries == null) {
            ancestries = new ArrayList<>();
            for (File file : Objects.requireNonNull(path.listFiles())) {
                Document doc = getDoc(file);
                NodeList classProperties = doc.getElementsByTagName("ancestry").item(0).getChildNodes();

                String name = ""; int hp = 0; Size size = Size.Medium; int speed=0; String bonuses=""; String penalties=""; List<Ability> feats = new ArrayList<>();List<Ability> heritages = new ArrayList<>(); String description = ""; List<Language> languages = new ArrayList<>();List<Language> bonusLanguages = new ArrayList<>();

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
                        case "Languages":
                            for (String s : trim.split(",")) {
                                languages.add(Language.valueOf(s.trim()));
                            }
                            break;
                        case "BonusLanguages":
                            for (String s : trim.split(",")) {
                                if(s.trim().equals("All")){
                                    bonusLanguages.addAll(Arrays.asList(Language.getChooseable()));
                                    break;
                                }
                                bonusLanguages.add(Language.valueOf(s.trim()));
                            }
                            break;
                        case "Size":
                            size = Size.valueOf(camelCaseWord(trim));
                            break;
                        case "Speed":
                            speed = Integer.parseInt(trim);
                            break;
                        case "AbilityBonuses":
                            bonuses = trim;
                            break;
                        case "AbilityPenalties":
                            penalties = trim;
                        case "Feats":
                            NodeList[] nodeLists = {curr.getElementsByTagName("Ability"),curr.getElementsByTagName("AbilitySet")};
                            for (NodeList featNodes : nodeLists) {
                                for (int j = 0; j < featNodes.getLength(); j++) {
                                    if (((Element) featNodes.item(j)).getAttribute("type").trim().toLowerCase().equals("heritage")) {
                                        heritages.add(makeAbility((Element) featNodes.item(j), ((Element) featNodes.item(j)).getAttribute("name")));
                                    } else {
                                        feats.add(makeAbility((Element) featNodes.item(j), ((Element) featNodes.item(j)).getAttribute("name")));
                                    }
                                }
                            }
                    }
                }

                ancestries.add(new Ancestry(name, description, languages, bonusLanguages, hp, size, speed, getAbilityMods(bonuses, penalties, Type.Ancestry), feats, heritages));
            }
        }
        return Collections.unmodifiableList(ancestries);
    }
}
