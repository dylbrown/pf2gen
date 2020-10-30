package model.equipment;

import freemarker.template.*;
import model.ability_slots.Choice;
import model.util.DataTemplateMap;
import model.util.TransformationMap;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ItemInstanceChoices extends ItemInstanceExtension<BaseItemChoices> {
    private final Map<String, Choice<?>> choices;

    protected ItemInstanceChoices(ItemInstance instance, BaseItemChoices baseItemChoices) {
        super(instance, baseItemChoices);
        choices = baseItemChoices.getChoices().stream().map(UnmodifiableChoice::copyBaseChoice).collect(Collectors.toUnmodifiableMap(
                Choice::getName,
                Function.identity()
        ));
    }

    public Map<String, Choice<?>> getChoices() {
        return choices;
    }

    @ItemDecorator
    public String getName(String name) {
        try {
            Template nameTemplate = new Template("Name", name, BaseItemChoices.cfg);
            StringWriter results = new StringWriter();
            nameTemplate.process(new DataTemplateMap(new TransformationMap<>(choices, choiceConverter), BaseItemChoices.cfg.getObjectWrapper()), results);
            return results.toString();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            return name;
        }
    }

    @ItemDecorator
    public String getDescription(String description) {
        try {
            Template descTemplate = new Template("Description", description, BaseItemChoices.cfg);
            StringWriter results = new StringWriter();
            descTemplate.process(new DataTemplateMap(new TransformationMap<>(choices, choiceConverter), BaseItemChoices.cfg.getObjectWrapper()), results);
            return results.toString();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            return description;
        }
    }
    private static final Function<Choice<?>, TemplateModel> choiceConverter = c->{
        if(c == null)
            return null;
        ObjectWrapper objectWrapper = BaseItemChoices.cfg.getObjectWrapper();
        if(c.getSelections().size() == 0) {
            try {
                return objectWrapper.wrap(c.getName());
            } catch (TemplateModelException e) {
                e.printStackTrace();
            }
        } else if(c.getMaxSelections() == 1) {
            try {
                return objectWrapper.wrap(c.getSelections().get(0));
            } catch (TemplateModelException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return objectWrapper.wrap(c.getSelections());
            } catch (TemplateModelException e) {
                e.printStackTrace();
            }
        }
        return null;
    };
}
