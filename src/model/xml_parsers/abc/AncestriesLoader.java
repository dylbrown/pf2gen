package model.xml_parsers.abc;

import model.abc.Ancestry;
import model.data_managers.sources.SourceConstructor;
import model.enums.Language;
import model.enums.Size;
import model.enums.Type;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

import static model.util.StringUtils.camelCaseWord;

public class AncestriesLoader extends ABCLoader<Ancestry, Ancestry.Builder> {

    static{
        source = element -> {
            if (element.getAttribute("type").trim().toLowerCase().equals("heritage"))
                return Type.Heritage;
            else return Type.Ancestry;
        };
    }

    private String bonuses, penalties;

    public AncestriesLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
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
            case "Senses":
                builder.addSenses(trim.split(" ?, ?"));
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
    protected Ancestry parseItem(String filename, Element item) {
        NodeList classProperties = item.getChildNodes();

        Ancestry.Builder builder = new Ancestry.Builder();
        this.bonuses = ""; this.penalties = "";

        for(int i=0; i<classProperties.getLength(); i++) {
            if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) classProperties.item(i);
            parseElement(curr, curr.getTextContent().trim(), builder);
        }
        builder.setAbilityMods(getAbilityMods(bonuses, penalties, Type.Ancestry));
        return builder.build();
    }
}
