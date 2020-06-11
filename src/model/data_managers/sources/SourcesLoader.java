package model.data_managers.sources;

import model.xml_parsers.*;
import model.xml_parsers.abc.AncestriesLoader;
import model.xml_parsers.abc.BackgroundsLoader;
import model.xml_parsers.abc.PClassesLoader;
import model.xml_parsers.equipment.ArmorLoader;
import model.xml_parsers.equipment.EquipmentLoader;
import model.xml_parsers.equipment.WeaponsLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;

public class SourcesLoader extends FileLoader<Source> {

    public SourcesLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }

    static {
        SourceConstructor sourceConstructor = new SourceConstructor("index.pfdyl", false);
        INSTANCE = new SourcesLoader(sourceConstructor, new File("data/Core Rulebook"));
    }

    private static final SourcesLoader INSTANCE;

    public static SourcesLoader instance() {
        return INSTANCE;
    }

    private void parseElement(Element curr, Source.Builder builder) {
        String tagName = curr.getTagName().toLowerCase();
        switch (tagName) {
            case "name":
                builder.setName(curr.getTextContent().trim());
                break;
            case "shortname":
                builder.setShortName(curr.getTextContent().trim());
                break;
            case "ancestries":
                builder.setAncestries(new AncestriesLoader(getSourceConstructor(curr), getRoot()));
                break;
            case "backgrounds":
                builder.setBackgrounds(new BackgroundsLoader(getSourceConstructor(curr), getRoot()));
                break;
            case "classes":
                builder.setClasses(new PClassesLoader(getSourceConstructor(curr), getRoot()));
                break;
            case "equipment":
                builder.setEquipment(new EquipmentLoader(getSourceConstructor(curr), getRoot()));
                break;
            case "armor":
                builder.setArmor(new ArmorLoader(getSourceConstructor(curr), getRoot()));
            case "weapons":
                builder.setWeapons(new WeaponsLoader(getSourceConstructor(curr), getRoot()));
                break;
            case "feats":
                builder.setFeats(new FeatsLoader(getSourceConstructor(curr), getRoot()));
                break;
            case "setting":
                NodeList childNodes = curr.getChildNodes();
                for(int j = 0; j < childNodes.getLength(); j++) {
                    Node item = childNodes.item(j);
                    if(item.getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    switch (((Element) item).getAttribute("name").toLowerCase()) {
                        case "deities":
                            builder.setDeities(new DeitiesLoader(getSourceConstructor((Element) item), getRoot()));
                            break;
                        case "domains":
                            builder.setDomains(new DomainsLoader(getSourceConstructor((Element) item), getRoot()));
                    }
                }
                break;
            case "spells":
                builder.setSpells(new SpellsLoader(getSourceConstructor(curr), getRoot()));
                break;
        }
    }

    private SourceConstructor getSourceConstructor(Element curr) {
        NodeList categories = curr.getElementsByTagName("Category");
        boolean isMultiMulti = categories.getLength() > 0;
        NodeList children = curr.getElementsByTagName("*");
        if(children.getLength() > 0) {
            HashMap<String, String> map = new HashMap<>();
            for(int i = 0; i < children.getLength(); i++) {
                Element item = (Element) children.item(i);
                map.put(item.getAttribute("name").toLowerCase(), item.getAttribute("path"));
            }
            return new SourceConstructor(map, isMultiMulti);
        }
        return new SourceConstructor(curr.getAttribute("path"), true);
    }

    @Override
    protected Source parseItem(String filename, Element item) {
        NodeList classProperties = item.getChildNodes();

        Source.Builder builder = new Source.Builder();

        for(int i=0; i<classProperties.getLength(); i++) {
            if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) classProperties.item(i);
            parseElement(curr, builder);
        }
        return builder.build();
    }
}
