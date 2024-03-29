package model.xml_parsers.equipment;

import model.abilities.Ability;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Type;
import model.xml_parsers.AbilityLoader;
import model.xml_parsers.BloodlinesLoader;
import model.xml_parsers.MysteriesLoader;
import org.w3c.dom.Element;

import java.io.File;

public class ItemAbilityLoader extends AbilityLoader<Ability> {

    static{
        sources.put(ItemAbilityLoader.class, e -> Type.Item);
    }

    private final Source.Builder sourceBuilder;
    private BloodlinesLoader bloodlineLoader;
    private MysteriesLoader mysteryLoader;

    public ItemAbilityLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
        this.sourceBuilder = sourceBuilder;
    }

    @Override
    protected Ability parseItem(File file, Element item) {
        if(file.getName().toLowerCase().contains("bloodline"))
            return makeBloodline(item);
        if(file.getName().toLowerCase().contains("mystery"))
            return makeMystery(item);
        return makeAbility(item,  item.getAttribute("name")).build();
    }

    private Ability makeBloodline(Element item) {
        if (bloodlineLoader == null)
            bloodlineLoader = new BloodlinesLoader(sourceBuilder);
        return bloodlineLoader.makeBloodline(item);
    }

    private Ability makeMystery(Element item) {
        if (mysteryLoader == null)
            mysteryLoader = new MysteriesLoader(sourceBuilder);
        return mysteryLoader.makeMystery(item);
    }
}
