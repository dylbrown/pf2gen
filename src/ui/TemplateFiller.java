package ui;

import freemarker.cache.TemplateLookupContext;
import freemarker.cache.TemplateLookupResult;
import freemarker.cache.TemplateLookupStrategy;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.*;
import freemarker.template.*;
import javafx.beans.value.ObservableValue;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.Activity;
import model.ability_scores.AbilityScore;
import model.enums.Attribute;
import model.enums.Proficiency;
import model.equipment.Weapon;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static ui.Main.character;
class SignedTemplateNumberFormatFactory extends TemplateNumberFormatFactory {
    static final SignedTemplateNumberFormatFactory INSTANCE = new SignedTemplateNumberFormatFactory();
    private SignedTemplateNumberFormatFactory(){}
    @Override
    public TemplateNumberFormat get(String s, Locale locale, Environment environment) {
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
        File file = new File("data/");
        if(file.exists()) {
            try {
                cfg.setDirectoryForTemplateLoading(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            cfg.setTemplateLoader(new URLTemplateLoader() {
                @Override
                protected URL getURL(String s) {
                    try {
                        return new URL("https://dylbrown.github.io/pf2gen_data/data/"+s);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
        }
        cfg.setTemplateLookupStrategy(new TemplateLookupStrategy() {
            @Override
            public TemplateLookupResult lookup(TemplateLookupContext ctx) throws IOException {
                return ctx.lookupWithAcquisitionStrategy(ctx.getTemplateName());
            }
        });
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
                        character.scores().getMod(AbilityScore.valueOf(s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase())),
                        ()->false));
        root.computeIfAbsent("abilityScore", (key)->
                new FunctionalInterfaceHash((s)->
                        character.scores().getScore(AbilityScore.valueOf(s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase())),
                        ()->false));
        root.computeIfAbsent("items", (key)-> character.inventory().getItems().values());
        root.put("skills", getSkills());
        root.computeIfAbsent("classes", (key)->getClasses());
        List<Ability> abilities = flattenAbilities(character.abilities().getAbilities());
        abilities.sort(((o1, o2) -> {
            if (o1 instanceof Activity && !(o2 instanceof Activity)) return -1;
            if (o2 instanceof Activity && !(o1 instanceof Activity)) return 1;
            return o1.toString().compareTo(o2.toString());
        }));
        root.put("abilities", abilities);
    }

    private List<Ability> flattenAbilities(List<Ability> abilities) {
        List<Ability> items = new ArrayList<>();
        for (Ability ability: abilities) {
            if(!(ability instanceof AbilitySet))
                items.add(ability);
            else
                items.addAll(flattenAbilities(((AbilitySet) ability).getAbilities()));

        }
        return items;
    }

    private Map<String, Class> getClasses() {
        HashMap<String, Class> map = new HashMap<>();
        map.put("weapon", Weapon.class);
        map.put("ability", Ability.class);
        map.put("abilitySet", AbilitySet.class);
        map.put("activity", Activity.class);
        return map;
    }

    private class FunctionalInterfaceHash implements TemplateHashModel {
        private Function<String, Object> getter;
        private Supplier<Boolean> empty;
        private FunctionalInterfaceHash(Function<String, Object> getter, Supplier<Boolean> empty){
            this.getter = getter;
            this.empty = empty;
        }
        @Override
        public TemplateModel get(String s) throws TemplateModelException { return cfg.getObjectWrapper().wrap(getter.apply(s)); }
        @Override
        public boolean isEmpty() { return empty.get(); }
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

        public Attribute getName() { return name; }

        public int getMod() {return mod;}
    }
}
