package model.xml_parsers.equipment;

import model.abilities.Ability;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Type;
import model.xml_parsers.AbilityLoader;
import model.xml_parsers.BloodlinesLoader;
import org.w3c.dom.Element;

import java.io.File;

public class ItemAbilityLoader extends AbilityLoader<Ability> {

    static{
        sources.put(ItemAbilityLoader.class, e -> Type.Item);
    }

    public ItemAbilityLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected Ability parseItem(File file, Element item) {
        if(file.getName().toLowerCase().contains("bloodline"))
            return BloodlinesLoader.makeBloodlineStatic(item);
        return makeAbility(item,  item.getAttribute("name")).build();
    }
}
