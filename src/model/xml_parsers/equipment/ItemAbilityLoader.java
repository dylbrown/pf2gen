package model.xml_parsers.equipment;

import model.abilities.Ability;
import model.data_managers.sources.SourceConstructor;
import model.enums.Type;
import model.xml_parsers.AbilityLoader;
import model.xml_parsers.BloodlinesLoader;
import org.w3c.dom.Element;

import java.io.File;

public class ItemAbilityLoader extends AbilityLoader<Ability> {

    static{
        source = e -> Type.Item;
    }

    public ItemAbilityLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }

    @Override
    protected Ability parseItem(String filename, Element item) {
        if(filename.toLowerCase().contains("bloodline"))
            return BloodlinesLoader.makeBloodline(item);
        return makeAbility(item,  item.getAttribute("name"));
    }
}
