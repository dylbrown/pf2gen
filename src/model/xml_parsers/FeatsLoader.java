package model.xml_parsers;

import model.abilities.Ability;
import model.abilities.ArchetypeExtension;
import model.ability_slots.DynamicFilledSlot;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.data_managers.sources.TypeSourceConstructor;
import model.enums.Type;
import model.util.ObjectNotFoundException;
import model.util.Pair;
import model.util.StringUtils;
import model.xml_parsers.abc.PClassesLoader;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Objects;

public class FeatsLoader extends AbilityLoader<Ability> {

    static{
        sources.put(FeatsLoader.class, e-> {
            if(e.getAttribute("type").equalsIgnoreCase("heritage"))
                return Type.Heritage;
            return null;
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
        if (builder.getType() == null && sourceConstructor instanceof TypeSourceConstructor) {
            String type = ((TypeSourceConstructor) sourceConstructor).getObjectType(category);
            if (type != null && !type.isBlank())
                builder.setType(Type.valueOf(StringUtils.capitalize(type.trim())));
        }
        if(builder.getType() == null) {
            if (!category.isBlank()) {
                if (category.equalsIgnoreCase("archetype"))
                    category = "class";
                builder.setType(Objects.requireNonNullElse(Type.robustValueOf(category), Type.General));
            } else builder.setType(Type.General);
        }
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
