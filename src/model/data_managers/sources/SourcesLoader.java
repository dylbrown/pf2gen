package model.data_managers.sources;

import model.player.SourcesManager;
import model.util.StringUtils;
import model.xml_parsers.*;
import model.xml_parsers.abc.AncestriesLoader;
import model.xml_parsers.abc.BackgroundsLoader;
import model.xml_parsers.abc.PClassesLoader;
import model.xml_parsers.equipment.ArmorLoader;
import model.xml_parsers.equipment.ItemLoader;
import model.xml_parsers.equipment.WeaponsLoader;
import model.xml_parsers.setting.DeitiesLoader;
import model.xml_parsers.setting.DomainsLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class SourcesLoader extends FileLoader<Source> {

    private static final SourcesLoader INSTANCE;
    public static final SourcesManager ALL_SOURCES;

    public static SourcesLoader instance() {
        return INSTANCE;
    }

    static {
        Map<String, List<String>> locations = new HashMap<>();
        File[] data = new File("data").listFiles();
        if(data != null) {
            for (File file : data) {
                if (file.isDirectory() && !file.getName().startsWith(".")) {
                    locations.put(StringUtils.clean(file.getName()), Collections.singletonList(file.getName() + "/index.pfdyl"));
                }
            }
        }
        try {
            URL url = new URL("https://dylbrown.github.io/pf2gen_data/data/index.txt");
            if(((HttpURLConnection) url.openConnection()).getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                reader.lines().forEach(s->locations.putIfAbsent(StringUtils.clean(s), Collections.singletonList(s+"/index.pfdyl")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SourceConstructor sourceConstructor = new SourceConstructor(locations, false);
        INSTANCE = new SourcesLoader(sourceConstructor, new File("data"));
        ALL_SOURCES = new SourcesManager(INSTANCE.getAll().values());
    }

    private SourcesLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }

    private void parseElement(File parentFile, Element curr, Source.Builder builder) {
        String tagName = curr.getTagName().toLowerCase();
        switch (tagName) {
            case "name":
                builder.setName(curr.getTextContent().trim());
                break;
            case "shortname":
                builder.setShortName(curr.getTextContent().trim());
                break;
            case "description":
                builder.setDescription(curr.getTextContent().trim());
                break;
            case "dependencies":
                NodeList childNodes = curr.getChildNodes();
                for(int j = 0; j < childNodes.getLength(); j++) {
                    Node item = childNodes.item(j);
                    if(item.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    builder.addDependency(item.getTextContent().trim());
                }
                break;
            case "category":
                builder.setCategory(curr.getTextContent().trim());
                break;
            case "subcategory":
                builder.setSubCategory(curr.getTextContent().trim());
                break;
            case "ancestries":
                builder.addLoader(AncestriesLoader.class,
                        new AncestriesLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "backgrounds":
                builder.addLoader(BackgroundsLoader.class,
                        new BackgroundsLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "classes":
                builder.addLoader(PClassesLoader.class,
                        new PClassesLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "equipment":
            case "items":
                builder.addLoader(ItemLoader.class,
                        new ItemLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "armor":
                builder.addLoader(ArmorLoader.class,
                        new ArmorLoader(getSourceConstructor(curr), parentFile, builder));
            case "weapons":
                builder.addLoader(WeaponsLoader.class,
                        new WeaponsLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "feats":
                builder.addLoader(FeatsLoader.class,
                        new FeatsLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "choices":
                builder.addLoader(ChoicesLoader.class,
                        new ChoicesLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "setting":
                childNodes = curr.getChildNodes();
                for(int j = 0; j < childNodes.getLength(); j++) {
                    Node item = childNodes.item(j);
                    if(item.getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    switch (((Element) item).getAttribute("name").toLowerCase()) {
                        case "deities":
                            builder.addLoader(DeitiesLoader.class,
                                    new DeitiesLoader(getSourceConstructor((Element) item), parentFile, builder));
                            break;
                        case "domains":
                            builder.addLoader(DomainsLoader.class,
                                    new DomainsLoader(getSourceConstructor((Element) item), parentFile, builder));
                        case "languages":
                            builder.addLoader(LanguagesLoader.class,
                                    new LanguagesLoader(getSourceConstructor((Element) item), parentFile, builder));
                    }
                }
                break;
            case "spells":
                builder.addLoader(SpellsLoader.class,
                        new SpellsLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "creatures":
                builder.addLoader(CreatureLoader.class,
                        new CreatureLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "creaturefamilies":
                builder.addLoader(CreatureFamilyLoader.class,
                        new CreatureFamilyLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "templates":
                builder.addLoader(TemplatesLoader.class,
                        new TemplatesLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "traits":
                builder.addLoader(TraitsLoader.class,
                        new TraitsLoader(getSourceConstructor(curr), parentFile, builder));
                break;
            case "senses":
                builder.addLoader(SensesLoader.class,
                        new SensesLoader(getSourceConstructor(curr), parentFile, builder));
                break;
        }
    }

    private SourceConstructor getSourceConstructor(Element curr) {
        NodeList categories = curr.getElementsByTagName("Category");
        boolean isMultiMulti = categories.getLength() > 0;
        if(curr.getElementsByTagName("*").getLength() > 0) {
            NodeList children = curr.getChildNodes();
            HashMap<String, List<String>> map = new HashMap<>();
            HashMap<String, String> typeMap = new HashMap<>();
            for(int i = 0; i < children.getLength(); i++) {
                if(!(children.item(i) instanceof Element))
                    continue;
                Element item = (Element) children.item(i);
                String name = item.getAttribute("name").toLowerCase();
                map.put(name, loadCategory(item));
                String type = item.getAttribute("type");
                if(!type.isBlank()) {
                    typeMap.put(name, type);
                }
            }
            if(typeMap.size() > 0)
                return new TypeSourceConstructor(map ,typeMap, isMultiMulti);
            return new SourceConstructor(map, isMultiMulti);
        }
        return new SourceConstructor(curr.getAttribute("path"), true);
    }

    private List<String> loadCategory(Element category) {
        NodeList children = category.getChildNodes();
        if(children.getLength() > 0) {
            List<String> files = new ArrayList<>();
            for(int i = 0; i < children.getLength(); i++) {
                if(!(children.item(i) instanceof Element))
                    continue;
                Element item = (Element) children.item(i);
                files.add(item.getTextContent());
            }
            return files;
        }
        return Collections.singletonList(category.getAttribute("path"));
    }

    @Override
    protected Source parseItem(File file, Element item) {
        NodeList classProperties = item.getChildNodes();

        Source.Builder builder = new Source.Builder();

        for(int i=0; i<classProperties.getLength(); i++) {
            if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) classProperties.item(i);
            parseElement(file.getParentFile(), curr, builder);
        }
        return builder.build();
    }
}
