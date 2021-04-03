package model.xml_parsers.abc;

import model.abc.Ancestry;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Language;
import model.enums.Size;
import model.enums.Type;
import model.util.ObjectNotFoundException;
import model.xml_parsers.SensesLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static model.util.StringUtils.camelCaseWord;

public class AncestriesLoader extends ACLoader<Ancestry, Ancestry.Builder> {

    static{
        sources.put(AncestriesLoader.class, element -> {
            if (element.getAttribute("type").trim().equalsIgnoreCase("heritage"))
                return Type.Heritage;
            else return Type.Ancestry;
        });
    }

    private String bonuses, penalties;

    public AncestriesLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
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
                Arrays.stream(trim.split(" ?, ?")).map(s->{
                    try {
                        return findFromDependencies("Sense", SensesLoader.class, s);
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).filter(Objects::nonNull).forEach(builder::addSenses);
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
            case "Abilities":
                NodeList featNodes = curr.getChildNodes();
                for (int j = 0; j < featNodes.getLength(); j++) {
                    if(featNodes.item(j) instanceof Element)
                        builder.addGrantedAbility(makeAbility((Element) featNodes.item(j),
                                ((Element) featNodes.item(j)).getAttribute("name")).build());
                }
                break;
            case "Feats":
                featNodes = curr.getChildNodes();
                for (int j = 0; j < featNodes.getLength(); j++) {
                    if(featNodes.item(j) instanceof Element)
                        if (((Element) featNodes.item(j)).getAttribute("type").trim().equalsIgnoreCase("heritage")) {
                            builder.addHeritage(makeAbility((Element) featNodes.item(j),
                                    ((Element) featNodes.item(j)).getAttribute("name")).build());
                        } else {
                            builder.addFeat(makeAbility((Element) featNodes.item(j),
                                    ((Element) featNodes.item(j)).getAttribute("name")).build());
                        }
                }
                break;
            default:
                super.parseElement(curr, trim, builder);
        }
    }

    @Override
    protected Ancestry parseItem(File file, Element item) {
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
