package model.xml_parsers;

import model.abilities.Ability;
import model.abilities.ArchetypeExtension;
import model.ability_slots.DynamicFilledSlot;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Type;
import model.util.ObjectNotFoundException;
import model.util.Pair;
import model.xml_parsers.abc.PClassesLoader;
import org.w3c.dom.Element;

import java.io.File;

public class FeatsLoader extends AbilityLoader<Ability> {

    static{
        sources.put(FeatsLoader.class, e-> {
            if(e.getAttribute("type").equalsIgnoreCase("heritage"))
                return Type.Heritage;
            return Type.General;
        });
    }

    private final Source.Builder sourceBuilder;
    private BloodlinesLoader bloodlineLoader;

    public FeatsLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
        this.sourceBuilder = sourceBuilder;
    }

    @Override
    protected Ability parseItem(File file, Element item) {
        return parseItem(file, item, null);
    }

    @Override
    protected Ability parseItem(File file, Element item, String category) {
        if(file.getName().toLowerCase().contains("bloodline") || category.equalsIgnoreCase("bloodline"))
            return makeBloodline(item);
        Ability.Builder builder = makeAbility(item, item.getAttribute("name"));
        //TODO: move these setters into makeAbility
        if(category.equals("ancestry") && builder.getType() != Type.Heritage)
            builder.setType(Type.Ancestry);
        if(category.equals("skill"))
            builder.setType(Type.Skill);
        if(builder.hasExtension(ArchetypeExtension.Builder.class)) {
            if(builder.getTraits().stream().anyMatch(t->t.getName().equals("Dedication")))
                builder.setType(Type.Dedication);
            else
                builder.setType(Type.Class);
        }
        while (!dynSlots.isEmpty()) {
            Pair<String, DynamicFilledSlot> pair = dynSlots.pop();
            try {
                pair.second.setPClass(
                        findFromDependencies("PClass", PClassesLoader.class, pair.first)
                );
            } catch (ObjectNotFoundException e) {
                e.printStackTrace();
            }
        }
        return builder.build();
    }

    private Ability makeBloodline(Element item) {
        if (bloodlineLoader == null)
            bloodlineLoader = new BloodlinesLoader(sourceBuilder);
        return bloodlineLoader.makeBloodline(item);
    }
}
