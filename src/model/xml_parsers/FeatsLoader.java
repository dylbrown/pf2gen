package model.xml_parsers;

import model.abilities.Ability;
import model.abilities.ArchetypeExtension;
import model.data_managers.sources.SourceConstructor;
import model.enums.Trait;
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
        sources.put(FeatsLoader.class, e-> Type.General);
    }

    public FeatsLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }

    @Override
    protected Ability parseItem(File file, Element item) {
        return parseItem(file, item, null);
    }

    @Override
    protected Ability parseItem(File file, Element item, String category) {
        if(file.getName().toLowerCase().contains("bloodline") || category.toLowerCase().equals("bloodline"))
            return BloodlinesLoader.makeBloodlineStatic(item);
        Ability.Builder builder = makeAbility(item, item.getAttribute("name"));
        if(category.equals("skill"))
            builder.setType(Type.Skill);
        if(builder.hasExtension(ArchetypeExtension.Builder.class)) {
            if(builder.getTraits().contains(Trait.Dedication))
                builder.setType(Type.Dedication);
            else
                builder.setType(Type.Class);
        }
        return builder.build();
    }
}
