package ui;

import freemarker.template.*;
import model.ability_scores.AbilityScore;
import model.enums.Attribute;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static ui.Main.character;

public class TemplateFiller {
    private static final Configuration cfg;
    private static final TemplateFiller instance;

    static{
        cfg = new Configuration(Configuration.VERSION_2_3_28);
        try {
            cfg.setDirectoryForTemplateLoading(new File("data/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);

        DefaultObjectWrapperBuilder owb = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_28);
        owb.setMethodAppearanceFineTuner((input, output) -> {
            String name = input.getMethod().getName();
            if(input.getMethod().getParameterCount() == 0 && name.substring(0, 3).equals("get")){
                try {
                    output.setExposeAsProperty(new PropertyDescriptor(name.substring(3).toLowerCase(),input.getMethod(), null));
                } catch (IntrospectionException e) {
                    e.printStackTrace();
                }
            }
        });

        cfg.setObjectWrapper(owb.build());

        instance = new TemplateFiller();
    }
    private Map<String, Object> root;
    private TemplateFiller() {
        root = new HashMap<>();
    }

    public static String getStatBlock() {
        return instance.statBlock();
    }

    private String statBlock() {
        updateContents();
        Template template;
        try {
            template = cfg.getTemplate("statblock.ftl");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        StringWriter results = new StringWriter();
        try {
            template.process(root, results);
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
        }
        return results.toString();
    }

    private void updateContents() {
        root.put("character", character);
        root.computeIfAbsent("attributes", (key)->
                new FunctionalInterfaceHash((s)->
                        character.attributes().getProficiency(Attribute.valueOf(s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase())).getValue().toString(),
                        ()->false));
        root.computeIfAbsent("abilityMod", (key)->
                new FunctionalInterfaceHash((s)->
                        character.getAbilityMod(AbilityScore.valueOf(s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase())).toString(),
                        ()->false));
        root.computeIfAbsent("items", (key)-> character.inventory().getItems().values());

    }

    private class FunctionalInterfaceHash implements TemplateHashModel {
        private Function<String, Object> getter;
        private Supplier<Boolean> empty;

        private FunctionalInterfaceHash(Function<String, Object> getter, Supplier<Boolean> empty){
            this.getter = getter;
            this.empty = empty;
        }


        @Override
        public TemplateModel get(String s) throws TemplateModelException {
            return cfg.getObjectWrapper().wrap(getter.apply(s));
        }

        @Override
        public boolean isEmpty() {
            return empty.get();
        }
    }
}
