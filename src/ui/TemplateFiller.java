package ui;

import freemarker.core.*;
import freemarker.template.*;
import javafx.beans.value.ObservableValue;
import model.ability_scores.AbilityScore;
import model.enums.Attribute;
import model.enums.Proficiency;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static ui.Main.character;
class SignedTemplateNumberFormatFactory extends TemplateNumberFormatFactory {
    static final SignedTemplateNumberFormatFactory INSTANCE = new SignedTemplateNumberFormatFactory();
    private SignedTemplateNumberFormatFactory(){};
    @Override
    public TemplateNumberFormat get(String s, Locale locale, Environment environment) throws TemplateValueFormatException {
        return SignedTemplateNumberFormat.INSTANCE;
    }
    private static class SignedTemplateNumberFormat extends TemplateNumberFormat {
        private static final SignedTemplateNumberFormat INSTANCE = new SignedTemplateNumberFormat();
        @Override
        public String formatToPlainText(TemplateNumberModel numberModel) throws TemplateValueFormatException, TemplateModelException {
            Number number = TemplateFormatUtil.getNonNullNumber(numberModel);
            if(number.intValue() < 0)
                return number.toString();
            else
                return "+"+number.toString();
        }

        @Override
        public boolean isLocaleBound() {
            return false;
        }

        @Override
        public String getDescription() {
            return "Adds sign of integer to front.";
        }
    }
}
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
        Map<String, TemplateNumberFormatFactory> customNumberFormats = new HashMap<>();
        customNumberFormats.put("s", SignedTemplateNumberFormatFactory.INSTANCE);
        cfg.setCustomNumberFormats(customNumberFormats);
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
                        character.getTotalMod(Attribute.valueOf(s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase())),
                        ()->false));
        root.computeIfAbsent("abilityMod", (key)->
                new FunctionalInterfaceHash((s)->
                        character.getAbilityScore(AbilityScore.valueOf(s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase())),
                        ()->false));
        root.computeIfAbsent("items", (key)-> character.inventory().getItems().values());
        root.put("skills", getSkills());
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

    private List<SkillEntry> getSkills() {
        List<SkillEntry> entries = new ArrayList<>();
        for (Map.Entry<Attribute, ObservableValue<Proficiency>> entry : character.attributes().getProficiencies().entrySet()) {
            if(Arrays.asList(Attribute.getSkills()).contains(entry.getKey()) && entry.getValue().getValue() != Proficiency.Untrained)
                entries.add(new SkillEntry(entry.getKey(), character.getTotalMod(entry.getKey())));
        }
        return entries;
    }

    public class SkillEntry {
        private Attribute name;
        private int mod;

        private SkillEntry(Attribute name, int mod) {
            this.name = name;
            this.mod = mod;
        }

        public Attribute getName() {
            return name;
        }

        public int getMod() {
            return mod;
        }
    }
}
