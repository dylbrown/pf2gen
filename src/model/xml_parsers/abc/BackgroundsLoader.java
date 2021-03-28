package model.xml_parsers.abc;

import model.abc.Background;
import model.abilities.Ability;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.attributes.AttributeModSingleChoice;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Proficiency;
import model.enums.Type;
import model.util.ObjectNotFoundException;
import model.xml_parsers.FeatsLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static model.util.StringUtils.capitalize;

public class BackgroundsLoader extends ABCLoader<Background, Background.Builder> {
    private List<Background> backgrounds;

    static{
        sources.put(BackgroundsLoader.class, e -> Type.Background);
    }

    public BackgroundsLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    void parseElement(Element curr, String trim, Background.Builder builder) {
        switch (curr.getTagName()) {
            case "Feat":
                builder.setFeat(trim);
                builder.setAbilityFunction(
                        name->{
                            Ability a = null;
                            try {
                                a = findFromDependencies("Ability", FeatsLoader.class, name);
                            } catch (ObjectNotFoundException e) {
                                e.printStackTrace();
                            }
                            return a;
                        });
                break;
            case "Skill":
                AttributeMod[] mods = {null, null};
                String[] split = capitalize(trim).split(",");
                for(int k=0; k<2; k++){
                    String[] orCheck = split[k].trim().split(" [oO]r ");
                    if(orCheck.length > 1) {
                        Attribute[] attributes = new Attribute[orCheck.length];
                        for (int l=0; l<orCheck.length; l++) {
                            attributes[l] = Attribute.valueOf(orCheck[l]);
                        }

                        mods[k] = new AttributeModSingleChoice(Arrays.asList(attributes), Proficiency.Trained);
                    }else{
                        mods[k] = new AttributeMod(Attribute.valueOf(split[k]), Proficiency.Trained);
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
