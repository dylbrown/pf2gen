package ui.ftl;

import freemarker.cache.*;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import model.abilities.Ability;
import model.abilities.AbilitySetExtension;
import model.abilities.ActivityExtension;
import model.enums.Slot;
import model.equipment.armor.Armor;
import model.equipment.armor.Shield;
import model.equipment.weapons.RangedWeapon;
import model.equipment.weapons.Weapon;
import model.spells.CasterType;
import ui.ftl.wrap.CharacterWrapper;
import ui.ftl.wrap.PF2GenObjectWrapper;

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

    public static ObjectWrapper getWrapper(){
        return cfg.getObjectWrapper();
    }

    public static TemplateFiller getInstance() {
        return instance;
    }


    static{
        cfg = new Configuration(Configuration.VERSION_2_3_28);
        if(new File("/data").isDirectory()) {
            try {
                cfg.setTemplateLoader(new FileTemplateLoader(new File("/data")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            cfg.setTemplateLoader(new URLTemplateLoader() {
                @Override
                protected URL getURL(String s) {
                    try {
                        return new URL("https://dylbrown.github.io/pf2gen_data/"+s);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
        }
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



        cfg.setObjectWrapper(new PF2GenObjectWrapper(Configuration.VERSION_2_3_29));
        Map<String, TemplateNumberFormatFactory> customNumberFormats = new HashMap<>();
        customNumberFormats.put("s", SignedTemplateNumberFormatFactory.INSTANCE);
        cfg.setCustomNumberFormats(customNumberFormats);
        instance = new TemplateFiller();
    }
    private Map<String, Object> root;
    private TemplateFiller() {
        root = new HashMap<>();
        wrapper = new CharacterWrapper(character, cfg.getObjectWrapper());
        root.put("character", wrapper);
        root.put("Weapon", Weapon.class);
        root.put("RangedWeapon", RangedWeapon.class);
        root.put("Armor", Armor.class);
        root.put("Shield", Shield.class);
        root.put("Ability", Ability.class);
        root.put("AbilitySet", AbilitySetExtension.class);
        root.put("Activity", ActivityExtension.class);
        try {
            root.put("Slot", ((BeansWrapper)cfg.getObjectWrapper()).getEnumModels().get(Slot.class.getName()));
            root.put("CasterType", ((BeansWrapper)cfg.getObjectWrapper()).getEnumModels().get(CasterType.class.getName()));
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
    }

    public static String getStatBlock() {
        return instance.getSheet("sheets/statblock.ftl");
    }

    public String getSheet(String templatePath) {
        wrapper.refresh();
        Template template;
        try {
            template = cfg.getTemplate(templatePath);
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
