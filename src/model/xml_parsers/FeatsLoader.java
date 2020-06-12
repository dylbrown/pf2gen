package model.xml_parsers;

import model.abilities.Ability;
import model.data_managers.sources.SourceConstructor;
import model.enums.Type;
import org.w3c.dom.Element;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatsLoader extends AbilityLoader<Ability> {
    private final Map<String, List<Ability>> featsMap = new HashMap<>();
    private List<Ability> feats;

    static{
        source = e-> Type.General;
    }

    public FeatsLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }

    @Override
    protected Ability parseItem(File file, Element item) {
        if(file.getName().toLowerCase().contains("bloodline"))
            return BloodlinesLoader.makeBloodline(item);
        return makeAbility(item,  item.getAttribute("name"));
    }
}
