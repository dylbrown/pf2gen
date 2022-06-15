package model.items;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import model.ability_slots.Choice;
import model.util.DataTemplateMap;
import ui.ftl.wrap.PF2GenObjectWrapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ui.ftl.wrap.PF2GenObjectWrapper.CUSTOM_FORMATS;

public class BaseItemChoices extends ItemExtension {
    private final List<UnmodifiableChoice<?>> choices;
    static final Configuration cfg;
    static {
        cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setObjectWrapper(new PF2GenObjectWrapper(Configuration.VERSION_2_3_29));
        cfg.setCustomNumberFormats(CUSTOM_FORMATS);
    }
    protected BaseItemChoices(Builder builder, Item baseItem) {
        super(baseItem);
        this.choices = builder.choices;
    }

    @Override
    public void applyToCreatedInstance(ItemInstance instance) {
        if(!instance.getSourceItem().equals(getItem()))
            return;
        instance.addInstanceExtension(ItemInstanceChoices.class, BaseItemChoices.class);
    }

    public List<UnmodifiableChoice<?>> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    @ItemDecorator @BaseItemOnly
    public String getName(String name) {
        try {
            Template nameTemplate = new Template("Name", name, BaseItemChoices.cfg);
            StringWriter results = new StringWriter();
            nameTemplate.process(new DataTemplateMap(Collections.emptyMap(), cfg.getObjectWrapper()), results);
            return results.toString();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            assert(false);
            return name;
        }
    }

    @ItemDecorator @BaseItemOnly
    public String getDescription(String description) {
        try {
            Template descTemplate = new Template("Description", description, cfg);
            StringWriter results = new StringWriter();
            descTemplate.process(new DataTemplateMap(Collections.emptyMap(), cfg.getObjectWrapper()), results);
            return results.toString();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            assert(false);
            return description;
        }
    }

    public static class Builder extends ItemExtension.Builder {
        private List<UnmodifiableChoice<?>> choices = Collections.emptyList();

        public <T> void addChoice(Choice<T> choice) {
            if(choices.size() == 0) choices = new ArrayList<>();
            choices.add(new UnmodifiableChoice<>(choice));
        }

        @Override
        public ItemExtension build(Item baseItem) {
            return new BaseItemChoices(this, baseItem);
        }
    }
}
