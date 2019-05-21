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
import java.util.Objects;

public class BackgroundsLoader extends FileLoader<Background> {
    private List<Background> backgrounds;

    public BackgroundsLoader() {
        this.path = new File("data/backgrounds");
    }

    @Override
    public List<Background> parse() {
        if(backgrounds == null) {
            backgrounds = new ArrayList<>();
            for (File file : Objects.requireNonNull(path.listFiles())) {
                Document doc = getDoc(file);
                NodeList classProperties = doc.getElementsByTagName("background").item(0).getChildNodes();

                String name = "";
                String desc = "";
                Attribute skill=null;
                String data="";
                String bonuses="";

                for(int i=0; i<classProperties.getLength(); i++) {
                    if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element curr = (Element) classProperties.item(i);
                    switch(curr.getTagName()){
                        case "Name":
                            name = curr.getTextContent().trim();
                            break;
                        case "Description":
                            desc = curr.getTextContent().trim();
                            break;
                        case "Skill":
                            if(camelCaseWord(curr.getTextContent().trim()).substring(0, 4).equals("Lore")) {
                                skill = Attribute.Lore;
                                data = camelCaseWord(curr.getTextContent().trim()).substring(4).trim().replaceAll("[()]", "");
                            }else{
                                skill = Attribute.valueOf(camelCaseWord(curr.getTextContent().trim()));
                            }
                            break;
                        case "AbilityBonuses":
                            bonuses = curr.getTextContent().trim();
                            break;
                    }
                }
                backgrounds.add(new Background(name, bonuses, desc, getAbilityMods(bonuses, "", Type.Background), skill, data));
            }
        }
        return Collections.unmodifiableList(backgrounds);
    }
}
