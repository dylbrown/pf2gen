package model.xml_parsers.abc;

import model.abc.Background;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.attributes.AttributeModSingleChoice;
import model.data_managers.sources.SourceConstructor;
import model.enums.Proficiency;
import model.enums.Type;
import model.util.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.List;

import static model.util.StringUtils.camelCase;
import static model.util.StringUtils.camelCaseWord;

public class BackgroundsLoader extends ABCLoader<Background, Background.Builder> {
    private List<Background> backgrounds;

    static{
        sources.put(BackgroundsLoader.class, e -> Type.Background);
    }

    public BackgroundsLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }

    @Override
    void parseElement(Element curr, String trim, Background.Builder builder) {
        switch (curr.getTagName()) {
            case "Feat":
                builder.setFeat(trim);
                break;
            case "Skill":
                AttributeMod[] mods = {null, null};
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
                builder.setMod1(mods[0]);
                builder.setMod2(mods[1]);
                break;
            case "AbilityBonuses":
                builder.setAbilityMods(getAbilityMods(trim, "", Type.Background));
                break;
            default:
                super.parseElement(curr, trim, builder);
        }
    }

    private Pair<Attribute, String> makeAttribute(String source) {
        if (source.contains("Lore")) {
            return new Pair<>(Attribute.Lore, source.replaceFirst("Lore ?\\(", "").replaceAll("\\).*", "").trim());
        }else return new Pair<>(Attribute.valueOf(camelCaseWord(source.trim())), "");
    }

    @Override
    protected Background parseItem(File file, Element item) {
        NodeList classProperties = item.getChildNodes();

        Background.Builder builder = new Background.Builder();

        for (int i = 0; i < classProperties.getLength(); i++) {
            if (classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) classProperties.item(i);
            parseElement(curr, curr.getTextContent().trim(), builder);
        }
        return builder.build();
    }
}
