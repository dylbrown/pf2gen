package model.data_managers.sources;

import model.xml_parsers.*;
import model.xml_parsers.abc.AncestriesLoader;
import model.xml_parsers.abc.BackgroundsLoader;
import model.xml_parsers.abc.PClassesLoader;
import model.xml_parsers.equipment.ArmorLoader;
import model.xml_parsers.equipment.EquipmentLoader;
import model.xml_parsers.equipment.WeaponsLoader;

public class Source {
    private final String name, shortName;
    private final AncestriesLoader ancestries;
    private final BackgroundsLoader backgrounds;
    private final PClassesLoader classes;
    private final EquipmentLoader equipment;
    private final ArmorLoader armor;
    private final WeaponsLoader weapons;
    private final FeatsLoader feats;
    private final DeitiesLoader deities;
    private final DomainsLoader domains;
    private final SpellsLoader spells;
    private final TemplatesLoader templates;

    private Source(Source.Builder builder) {
        this.name = builder.name;
        this.shortName = builder.shortName;
        this.ancestries = builder.ancestries;
        this.backgrounds = builder.backgrounds;
        this.classes = builder.classes;
        this.equipment = builder.equipment;
        this.armor = builder.armor;
        this.weapons = builder.weapons;
        this.feats = builder.feats;
        this.deities = builder.deities;
        this.domains = builder.domains;
        this.spells = builder.spells;
        this.templates = builder.templates;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getShortName() {
        return shortName;
    }

    public AncestriesLoader getAncestries() {
        return ancestries;
    }

    public BackgroundsLoader getBackgrounds() {
        return backgrounds;
    }

    public PClassesLoader getClasses() {
        return classes;
    }

    public EquipmentLoader getEquipment() {
        return equipment;
    }

    public ArmorLoader getArmor() {
        return armor;
    }

    public WeaponsLoader getWeapons() {
        return weapons;
    }

    public FeatsLoader getFeats() {
        return feats;
    }

    public DeitiesLoader getDeities() {
        return deities;
    }

    public DomainsLoader getDomains() {
        return domains;
    }

    public SpellsLoader getSpells() {
        return spells;
    }

    public TemplatesLoader getTemplates() {
        return templates;
    }

    public static class Builder {
        private String name, shortName;
        private AncestriesLoader ancestries;
        private BackgroundsLoader backgrounds;
        private PClassesLoader classes;
        private EquipmentLoader equipment;
        private ArmorLoader armor;
        private WeaponsLoader weapons;
        private FeatsLoader feats;
        private DeitiesLoader deities;
        private DomainsLoader domains;
        private SpellsLoader spells;
        private TemplatesLoader templates;

        public void setName(String name) {
            this.name = name;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public void setAncestries(AncestriesLoader ancestries) {
            this.ancestries = ancestries;
        }

        public void setBackgrounds(BackgroundsLoader backgrounds) {
            this.backgrounds = backgrounds;
        }

        public void setClasses(PClassesLoader classes) {
            this.classes = classes;
        }

        public void setEquipment(EquipmentLoader equipment) {
            this.equipment = equipment;
        }

        public void setArmor(ArmorLoader armor) {
            this.armor = armor;
        }

        public void setWeapons(WeaponsLoader weapons) {
            this.weapons = weapons;
        }

        public void setFeats(FeatsLoader feats) {
            this.feats = feats;
        }

        public void setDeities(DeitiesLoader deities) {
            this.deities = deities;
        }

        public void setDomains(DomainsLoader domains) {
            this.domains = domains;
        }

        public void setSpells(SpellsLoader spells) {
            this.spells = spells;
        }

        public void setTemplates(TemplatesLoader templates) {
            this.templates = templates;
        }

        public Source build() {
            return new Source(this);
        }
    }
}
