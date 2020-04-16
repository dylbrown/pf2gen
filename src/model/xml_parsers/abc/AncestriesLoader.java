package model.xml_parsers.abc;

import model.abc.Ancestry;
import model.enums.Language;
import model.enums.Size;
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

import static model.util.StringUtils.camelCaseWord;

public class AncestriesLoader extends ABCLoader<Ancestry, Ancestry.Builder> {

    private static final AncestriesLoader instance;
    static{instance = new AncestriesLoader();}

    private String bonuses, penalties;

    private AncestriesLoader() {
        path = new File("data/ancestries");
    }
    private List<Ancestry> ancestries;

    public static AncestriesLoader instance() {
        return instance;
    }

    @Override
    public List<Ancestry> parse() {
        if(ancestries == null) {
            ancestries = new ArrayList<>();
            for (Pair<Document, String> docEntry : getDocs(path)) {
                Document doc = docEntry.first;
                NodeList classProperties = doc.getElementsByTagName("ancestry").item(0).getChildNodes();

                Ancestry.Builder builder = new Ancestry.Builder();
                this.bonuses = ""; this.penalties = "";

                for(int i=0; i<classProperties.getLength(); i++) {
                    if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element curr = (Element) classProperties.item(i);
                    parseElement(curr, curr.getTextContent().trim(), builder);
                }
                builder.setAbilityMods(getAbilityMods(bonuses, penalties, Type.Ancestry));
                ancestries.add(builder.build());
            }
        }
        return Collections.unmodifiableList(ancestries);
    }

    @Override
    void parseElement(Element curr, String trim, Ancestry.Builder builder) {
        switch(curr.getTagName()){
            case "Languages":
                for (String s : trim.split(",")) {
                    builder.addLanguages(Language.valueOf(s.trim()));
                }
                break;
            case "BonusLanguages":
                for (String s : trim.split(",")) {
                    if(s.trim().equals("All")){
                        builder.addBonusLanguages(Language.getChooseable());
                        break;
                    }
                    builder.addBonusLanguages(Language.valueOf(s.trim()));
                }
                break;
            case "Size":
                builder.setSize(Size.valueOf(camelCaseWord(trim)));
                break;
            case "Speed":
                builder.setSpeed(Integer.parseInt(trim));
                break;
            case "AbilityBonuses":
                this.bonuses = trim;
                break;
            case "AbilityPenalties":
                this.penalties = trim;
                break;
            case "Feats":
                NodeList featNodes = curr.getChildNodes();
                for (int j = 0; j < featNodes.getLength(); j++) {
                    if(featNodes.item(j) instanceof Element)
                        if (((Element) featNodes.item(j)).getAttribute("type").trim().toLowerCase().equals("heritage")) {
                            builder.addHeritage(makeAbility((Element) featNodes.item(j),
                                    ((Element) featNodes.item(j)).getAttribute("name")));
                        } else {
                            builder.addFeat(makeAbility((Element) featNodes.item(j),
                                    ((Element) featNodes.item(j)).getAttribute("name")));
                        }
                }
                break;
            default:
                super.parseElement(curr, trim, builder);
        }
    }

    @Override
    protected Type getSource(Element element) {
        if (element.getAttribute("type").trim().toLowerCase().equals("heritage"))
            return Type.Heritage;
        else return Type.Ancestry;
    }
}
