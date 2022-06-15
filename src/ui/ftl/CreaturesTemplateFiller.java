package ui.ftl;

import freemarker.cache.*;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.template.*;
import model.creatures.Creature;
import ui.ftl.wrap.ObjectWrapperCreatures;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreaturesTemplateFiller {
    private final Configuration cfg;

    public ObjectWrapper getWrapper(){
        return cfg.getObjectWrapper();
    }
    private final Map<String, Object> root = new HashMap<>();
    public CreaturesTemplateFiller(List<Creature> creatures) {
        cfg = new Configuration(Configuration.VERSION_2_3_29);
        if(new File("/sheets").isDirectory()) {
            try {
                cfg.setTemplateLoader(new FileTemplateLoader(new File("/sheets")));
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
                        assert(false);
                    }
                    return null;
                }
            });
        }
        File file = new File("sheets/");
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
                        return new URL("https://dylbrown.github.io/pf2gen_data/sheets/"+s);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        assert(false);
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



        cfg.setObjectWrapper(new ObjectWrapperCreatures(Configuration.VERSION_2_3_29, creatures, root));
        Map<String, TemplateNumberFormatFactory> customNumberFormats = new HashMap<>();
        customNumberFormats.put("s", SignedTemplateNumberFormatFactory.INSTANCE);
        cfg.setCustomNumberFormats(customNumberFormats);
        root.put("creatures", creatures);
    }

    public String getSheet(String templatePath) {
        long startTime = System.currentTimeMillis();
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
        System.out.println(System.currentTimeMillis() - startTime + " ms");
        return results.toString();
    }
}
