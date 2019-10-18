package model.xml_parsers;

import model.AttributeMod;
import model.AttributeModSingleChoice;
import model.abc.Background;
import model.enums.Attribute;
import model.enums.Proficiency;
import model.enums.Type;
import model.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static model.util.StringUtils.camelCase;
import static model.util.StringUtils.camelCaseWord;

public class BackgroundsLoader extends FileLoader<Background> {
    private List<Background> backgrounds;

    private static BackgroundsLoader instance;
    static{instance = new BackgroundsLoader();}

    private BackgroundsLoader() {
        this.path = new File("data/backgrounds");
    }

    public static BackgroundsLoader instance() {
        return instance;
    }

    @Override
    public List<Background> parse() {
        if(backgrounds == null) {
            backgrounds = new ArrayList<>();
            for (Document doc : getDocs(path)) {
                NodeList backgroundNodes = doc.getElementsByTagName("background");
                for(int b=0; b<backgroundNodes.getLength(); b++) {
                    NodeList classProperties = backgroundNodes.item(b).getChildNodes();

                    String name = "";
                    String desc = "";
                    AttributeMod[] mods = {null, null};
                    String bonuses = "";
                    String bonusFeat = "";

                    for (int i = 0; i < classProperties.getLength(); i++) {
                        if (classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element curr = (Element) classProperties.item(i);
                        String trim = curr.getTextContent().trim();
                        switch (curr.getTagName()) {
                            case "Name":
                                name = trim;
                                break;
                            case "Description":
                                desc = trim;
                                break;
                            case "Feat":
                                bonusFeat = trim;
                                break;
                            case "Skill":
                                String[] split = camelCase(trim).split(",");
                                for(int k=0; k<2; k++){
                                    String[] orCheck = split[k].trim().split(" [oO]r ");
                                    if(orCheck.length > 1) {
                                        Attribute[] attributes = new Attribute[orCheck.length];
                                        for (int l=0; l<orCheck.length; l++) {
                                            attributes[l] = makeAttribute(orCheck[l]).first;
                                        }

                                        mods[k] = new AttributeModSingleChoice(attributes, Proficiency.Trained);
                                    }else{
                                        Pair<Attribute, String> pair = makeAttribute(split[k]);
                                        mods[k] = new AttributeMod(pair.first, Proficiency.Trained, pair.second);
                                    }
                                }
                                break;
                            case "AbilityBonuses":
                                bonuses = trim;
                                break;
                        }
                    }
                    backgrounds.add(new Background(name, bonuses, bonusFeat, desc, getAbilityMods(bonuses, "", Type.Background), mods[0], mods[1]));
                }
            }
        }
        return Collections.unmodifiableList(backgrounds);
    }

    @Override
    protected Type getSource() {
        return Type.Background;
    }

    private Pair<Attribute, String> makeAttribute(String source) {
        if (source.contains("Lore")) {
            return new Pair<>(Attribute.Lore, source.replaceFirst("Lore ?\\(", "").replaceAll("\\).*", ""));
        }else return new Pair<>(Attribute.valueOf(camelCaseWord(source.trim())), "");
    }
}
