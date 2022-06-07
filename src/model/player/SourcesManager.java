package model.player;

import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.Ability;
import model.creatures.Creature;
import model.creatures.CreatureFamily;
import model.data_managers.sources.MultiSourceLoader;
import model.data_managers.sources.Source;
import model.data_managers.sources.SpellsMultiSourceLoader;
import model.data_managers.sources.WeaponsMultiSourceLoader;
import model.enums.Sense;
import model.enums.Trait;
import model.items.Item;
import model.setting.Deity;
import model.setting.Domain;
import model.xml_parsers.*;
import model.xml_parsers.abc.AncestriesLoader;
import model.xml_parsers.abc.BackgroundsLoader;
import model.xml_parsers.abc.PClassesLoader;
import model.xml_parsers.equipment.ArmorLoader;
import model.xml_parsers.equipment.ItemLoader;
import model.xml_parsers.equipment.WeaponsLoader;
import model.xml_parsers.setting.DeitiesLoader;
import model.xml_parsers.setting.DomainsLoader;

import java.util.*;

public class SourcesManager {
    private final Set<Source> sources = new HashSet<>();
    private final MultiSourceLoader<Ancestry> ancestries = new MultiSourceLoader<>("Ancestry");
    private final MultiSourceLoader<Background> backgrounds = new MultiSourceLoader<>("Background");
    private final MultiSourceLoader<PClass> classes = new MultiSourceLoader<>("Class");
    private final MultiSourceLoader<Item> equipment = new MultiSourceLoader<>("Equipment");
    private final MultiSourceLoader<Item> armor = new MultiSourceLoader<>("Armor");
    private final WeaponsMultiSourceLoader weapons = new WeaponsMultiSourceLoader();
    private final MultiSourceLoader<Ability> feats = new MultiSourceLoader<>("Feat");
    private final MultiSourceLoader<Ability> choices = new MultiSourceLoader<>("Ability Choice");
    private final MultiSourceLoader<Deity> deities = new MultiSourceLoader<>("Deity");
    private final MultiSourceLoader<Domain> domains = new MultiSourceLoader<>("Domain");
    private final SpellsMultiSourceLoader spells = new SpellsMultiSourceLoader();
    private final MultiSourceLoader<Creature> creatures = new MultiSourceLoader<>("Creature");
    private final MultiSourceLoader<CreatureFamily> creatureFamilies = new MultiSourceLoader<>("Creature Family");
    private final MultiSourceLoader<TemplatesLoader.BuilderSupplier> templates = new MultiSourceLoader<>("Template");
    private final MultiSourceLoader<Trait> traits = new MultiSourceLoader<>("Trait");
    private final MultiSourceLoader<Sense> senses = new MultiSourceLoader<>("Sense");

    public SourcesManager() {}

    public SourcesManager(Collection<Source> sources) {
        for (Source source : sources) {
            add(source);
        }
    }

    public void add(Source source) {
        if(sources.contains(source))
            return;
        sources.add(source);
        addIfNotNull(ancestries, source.getLoader(AncestriesLoader.class));
        addIfNotNull(backgrounds, source.getLoader(BackgroundsLoader.class));
        addIfNotNull(classes, source.getLoader(PClassesLoader.class));
        addIfNotNull(equipment, source.getLoader(ItemLoader.class));
        addIfNotNull(armor, source.getLoader(ArmorLoader.class));
        addIfNotNull(weapons, source.getLoader(WeaponsLoader.class));
        addIfNotNull(feats, source.getLoader(FeatsLoader.class));
        addIfNotNull(choices, source.getLoader(ChoicesLoader.class));
        addIfNotNull(deities, source.getLoader(DeitiesLoader.class));
        addIfNotNull(domains, source.getLoader(DomainsLoader.class));
        addIfNotNull(spells, source.getLoader(SpellsLoader.class));
        addIfNotNull(creatures, source.getLoader(CreatureLoader.class));
        addIfNotNull(creatureFamilies, source.getLoader(CreatureFamilyLoader.class));
        addIfNotNull(templates, source.getLoader(TemplatesLoader.class));
        addIfNotNull(traits, source.getLoader(TraitsLoader.class));
        addIfNotNull(senses, source.getLoader(SensesLoader.class));
    }

    private <T> void addIfNotNull(MultiSourceLoader<T> multiSourceLoader, FileLoader<T> loader) {
        if(loader != null)
            multiSourceLoader.add(loader);
    }

    public MultiSourceLoader<Ancestry> ancestries() {
        return ancestries;
    }

    public MultiSourceLoader<Background> backgrounds() {
        return backgrounds;
    }

    public MultiSourceLoader<PClass> classes() {
        return classes;
    }

    public MultiSourceLoader<Item> equipment() {
        return equipment;
    }

    public MultiSourceLoader<Item> armor() {
        return armor;
    }

    public WeaponsMultiSourceLoader weapons() {
        return weapons;
    }

    public MultiSourceLoader<Ability> feats() {
        return feats;
    }

    public MultiSourceLoader<Ability> choices() {
        return choices;
    }

    public MultiSourceLoader<Deity> deities() {
        return deities;
    }

    public MultiSourceLoader<Domain> domains() {
        return domains;
    }

    public SpellsMultiSourceLoader spells() {
        return spells;
    }

    public MultiSourceLoader<Creature> creatures() {
        return creatures;
    }

    public MultiSourceLoader<CreatureFamily> creatureFamilies() {
        return creatureFamilies;
    }

    public MultiSourceLoader<TemplatesLoader.BuilderSupplier> templates() {
        return templates;
    }

    public MultiSourceLoader<Trait> traits() {
        return traits;
    }

    public MultiSourceLoader<Sense> senses() {
        return senses;
    }

    public Set<Source> getSources() {
        return Collections.unmodifiableSet(sources);
    }
}
