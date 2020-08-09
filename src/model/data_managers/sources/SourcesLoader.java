package model.data_managers.sources;

import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.Ability;
import model.creatures.Creature;
import model.creatures.CreatureFamily;
import model.enums.Trait;
import model.equipment.Equipment;
import model.equipment.armor.Armor;
import model.xml_parsers.*;
import model.xml_parsers.abc.AncestriesLoader;
import model.xml_parsers.abc.BackgroundsLoader;
import model.xml_parsers.abc.PClassesLoader;
import model.xml_parsers.equipment.ArmorLoader;
import model.xml_parsers.equipment.EquipmentLoader;
import model.xml_parsers.equipment.WeaponsLoader;
import model.xml_parsers.setting.DeitiesLoader;
import model.xml_parsers.setting.DomainsLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import setting.Deity;
import setting.Domain;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourcesLoader extends FileLoader<Source> {

    private final MultiSourceLoader<Ancestry> ancestriesLoader;
    private final MultiSourceLoader<Background> backgroundsLoader;
    private final MultiSourceLoader<PClass> classesLoader;
    private final MultiSourceLoader<Equipment> equipmentLoader;
    private final MultiSourceLoader<Armor> armorLoader;
    private final WeaponsMultiSourceLoader weaponsLoader;
    private final MultiSourceLoader<Ability> featsLoader;
    private final MultiSourceLoader<Ability> choicesLoader;
    private final MultiSourceLoader<Deity> deitiesLoader;
    private final MultiSourceLoader<Domain> domainsLoader;
    private final SpellsMultiSourceLoader spellsLoader;
    private final MultiSourceLoader<Creature> creaturesLoader;
    private final MultiSourceLoader<CreatureFamily> creatureFamiliesLoader;
    private final MultiSourceLoader<TemplatesLoader.BuilderSupplier> templatesLoader;
    private final MultiSourceLoader<Trait> traitsLoader;

    public SourcesLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
        List<AncestriesLoader> ancestries = new ArrayList<>();
        List<BackgroundsLoader> backgrounds = new ArrayList<>();
        List<PClassesLoader> classes = new ArrayList<>();
        List<EquipmentLoader> equipment = new ArrayList<>();
        List<ArmorLoader> armor = new ArrayList<>();
        List<WeaponsLoader> weapons = new ArrayList<>();
        List<FeatsLoader> feats = new ArrayList<>();
        List<ChoicesLoader> choices = new ArrayList<>();
        List<DeitiesLoader> deities = new ArrayList<>();
        List<DomainsLoader> domains = new ArrayList<>();
        List<SpellsLoader> spells = new ArrayList<>();
        List<CreatureLoader> creatures = new ArrayList<>();
        List<CreatureFamilyLoader> creatureFamilies = new ArrayList<>();
        List<TemplatesLoader> templates = new ArrayList<>();
        List<TraitsLoader> traits = new ArrayList<>();
        for (Source value : getAll().values()) {
            addIfNotNull(ancestries, value.getLoader(AncestriesLoader.class));
            addIfNotNull(backgrounds, value.getLoader(BackgroundsLoader.class));
            addIfNotNull(classes, value.getLoader(PClassesLoader.class));
            addIfNotNull(equipment, value.getLoader(EquipmentLoader.class));
            addIfNotNull(armor, value.getLoader(ArmorLoader.class));
            addIfNotNull(weapons, value.getLoader(WeaponsLoader.class));
            addIfNotNull(feats, value.getLoader(FeatsLoader.class));
            addIfNotNull(choices, value.getLoader(ChoicesLoader.class));
            addIfNotNull(deities, value.getLoader(DeitiesLoader.class));
            addIfNotNull(domains, value.getLoader(DomainsLoader.class));
            addIfNotNull(spells, value.getLoader(SpellsLoader.class));
            addIfNotNull(creatures, value.getLoader(CreatureLoader.class));
            addIfNotNull(creatureFamilies, value.getLoader(CreatureFamilyLoader.class));
            addIfNotNull(templates, value.getLoader(TemplatesLoader.class));
            addIfNotNull(traits, value.getLoader(TraitsLoader.class));
        }
        ancestriesLoader = new MultiSourceLoader<>(ancestries);
        backgroundsLoader = new MultiSourceLoader<>(backgrounds);
        classesLoader = new MultiSourceLoader<>(classes);
        equipmentLoader = new MultiSourceLoader<>(equipment);
        armorLoader = new MultiSourceLoader<>(armor);
        weaponsLoader = new WeaponsMultiSourceLoader(weapons);
        featsLoader = new MultiSourceLoader<>(feats);
        choicesLoader = new MultiSourceLoader<>(choices);
        deitiesLoader = new MultiSourceLoader<>(deities);
        domainsLoader = new MultiSourceLoader<>(domains);
        spellsLoader = new SpellsMultiSourceLoader(spells);
        creaturesLoader = new MultiSourceLoader<>(creatures);
        creatureFamiliesLoader = new MultiSourceLoader<>(creatureFamilies);
        templatesLoader = new MultiSourceLoader<>(templates);
        traitsLoader = new MultiSourceLoader<>(traits);
    }

    private <T> void addIfNotNull(List<T> list, T ancestries) {
        if(ancestries != null)
            list.add(ancestries);
    }

    static {
        Map<String, String> locations = new HashMap<>();
        locations.put("advanced_players_guide", "Advanced Player's Guide/index.pfdyl");
        locations.put("core_rulebook", "Core Rulebook/index.pfdyl");
        locations.put("bestiary_1", "Bestiary 1/index.pfdyl");
        locations.put("the_amar_vale", "The Amar Vale/index.pfdyl");
        // locations.put("extinction_curse", "Extinction Curse/index.pfdyl");
        SourceConstructor sourceConstructor = new SourceConstructor(locations, false);
        INSTANCE = new SourcesLoader(sourceConstructor, new File("data"));
    }

    private static final SourcesLoader INSTANCE;

    public static SourcesLoader instance() {
        return INSTANCE;
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
                builder.addLoader(EquipmentLoader.class,
                        new EquipmentLoader(getSourceConstructor(curr), parentFile, builder));
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
                NodeList childNodes = curr.getChildNodes();
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

    public MultiSourceLoader<Ancestry> ancestries() {
        return ancestriesLoader;
    }

    public MultiSourceLoader<Background> backgrounds() {
        return backgroundsLoader;
    }

    public MultiSourceLoader<PClass> classes() {
        return classesLoader;
    }

    public MultiSourceLoader<Equipment> equipment() {
        return equipmentLoader;
    }

    public MultiSourceLoader<Armor> armor() {
        return armorLoader;
    }

    public WeaponsMultiSourceLoader weapons() {
        return weaponsLoader;
    }

    public MultiSourceLoader<Ability> feats() {
        return featsLoader;
    }

    public MultiSourceLoader<Ability> choices() {
        return choicesLoader;
    }

    public MultiSourceLoader<Deity> deities() {
        return deitiesLoader;
    }

    public MultiSourceLoader<Domain> domains() {
        return domainsLoader;
    }

    public SpellsMultiSourceLoader spells() {
        return spellsLoader;
    }

    public MultiSourceLoader<Creature> creatures() {
        return creaturesLoader;
    }

    public MultiSourceLoader<CreatureFamily> creatureFamilies() {
        return creatureFamiliesLoader;
    }

    public MultiSourceLoader<TemplatesLoader.BuilderSupplier> templates() {
        return templatesLoader;
    }

    public MultiSourceLoader<Trait> traits() {
        return traitsLoader;
    }
}
