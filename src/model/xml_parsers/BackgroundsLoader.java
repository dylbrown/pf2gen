package model.xml_parsers;

import model.abc.Background;
import model.enums.Attribute;
import model.enums.Type;
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
                    Attribute skill1 = null; Attribute skill2 = null;
                    String data1 = ""; String data2 = "";
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
                                String[] split = camelCase(trim).split(", +");
                                if (split[0].substring(0, 4).equals("Lore")) {
                                    skill1 = Attribute.Lore;
                                    data1 = camelCase(split[0].substring(4).trim().replaceAll("[()]", ""));
                                } else {
                                    skill1 = Attribute.valueOf(camelCaseWord(split[0]));
                                }
                                if (split[1].substring(0, 4).equals("Lore")) {
                                    skill2 = Attribute.Lore;
                                    data2 = camelCase(split[1].substring(4).trim().replaceAll("[()]", ""));
                                } else {
                                    skill2 = Attribute.valueOf(camelCaseWord(split[1]));
                                }
                                break;
                            case "AbilityBonuses":
                                bonuses = trim;
                                break;
                        }
                    }
                    backgrounds.add(new Background(name, bonuses, bonusFeat, desc, getAbilityMods(bonuses, "", Type.Background), skill1, skill2, data1, data2));
                }
            }
        }
        return Collections.unmodifiableList(backgrounds);
    }

    @Override
    protected Type getSource() {
        return Type.Background;
    }
}
