package ui.ftl;

import freemarker.cache.TemplateLookupContext;
import freemarker.cache.TemplateLookupResult;
import freemarker.cache.TemplateLookupStrategy;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.Activity;
import model.enums.Slot;
import model.equipment.Armor;
import model.equipment.Shield;
import model.equipment.Weapon;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static ui.Main.character;
public class TemplateFiller {
    private static final Configuration cfg;
    private static final TemplateFiller instance;
    private final CharacterWrapper wrapper;


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
        wrapper = new CharacterWrapper(character, cfg);
        root.put("character", wrapper);
        root.put("Weapon", Weapon.class);
        root.put("Armor", Armor.class);
        root.put("Shield", Shield.class);
        root.put("Ability", Ability.class);
        root.put("AbilitySet", AbilitySet.class);
        root.put("Activity", Activity.class);
        try {
            root.put("Slot", ((BeansWrapper)cfg.getObjectWrapper()).getEnumModels().get(Slot.class.getName()));
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
    }

    public static String getStatBlock() {
        return instance.statBlock();
    }

    public static String getSheet() {
        return instance.sheet();
    }

    private String statBlock() {
        wrapper.refresh();
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

    private String sheet() {
        wrapper.refresh();
        Template template;
        try {
            template = cfg.getTemplate("csheet_jquery.ftl");
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
}
