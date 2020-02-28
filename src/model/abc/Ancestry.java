package model.abc;

import model.abilities.Ability;
import model.ability_scores.AbilityMod;
import model.enums.Language;
import model.enums.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Ancestry extends AC {
    public static final Ancestry NO_ANCESTRY = new Ancestry.Builder().build();
    private final Size size;
    private final int speed;
    private final List<Ability> heritages;
    private final List<Language> languages;
    private final List<Language> bonusLanguages;

    private Ancestry(Ancestry.Builder builder) {
        super(builder.name, builder.description, builder.abilityMods, builder.HP, builder.feats);
        this.size = builder.size;
        this.speed = builder.speed;
        this.heritages = builder.heritages;
        this.languages = builder.languages;
        this.bonusLanguages = builder.bonusLanguages;
    }

    public List<Ability> getHeritages() {
        return Collections.unmodifiableList(heritages);
    }

    public int getSpeed() {
        return speed;
    }

    public Size getSize() {
        return size;
    }

    public List<Language> getLanguages() {
        return Collections.unmodifiableList(languages);
    }

    public List<Language> getBonusLanguages() {
        return Collections.unmodifiableList(bonusLanguages);
    }

    public static class Builder {
        private String name = "";
        private int HP = 0;
        private Size size = Size.Medium;
        private int speed = 0;
        private List<AbilityMod> abilityMods = Collections.emptyList();
        private List<Ability> feats = new ArrayList<>();
        private List<Ability> heritages = new ArrayList<>();
        private String description = "";
        private List<Language> languages = new ArrayList<>();
        private List<Language> bonusLanguages = new ArrayList<>();

        public Ancestry build() {
            return new Ancestry(this);
        }

        public Ancestry.Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Ancestry.Builder setHP(int HP) {
            this.HP = HP;
            return this;
        }

        public Ancestry.Builder setSize(Size size) {
            this.size = size;
            return this;
        }

        public Ancestry.Builder setSpeed(int speed) {
            this.speed = speed;
            return this;
        }

        public Ancestry.Builder addFeat(Ability feat) {
            this.feats.add(feat);
            return this;
        }

        public Ancestry.Builder addHeritage(Ability heritage) {
            this.heritages.add(heritage);
            return this;
        }

        public Ancestry.Builder setAbilityMods(List<AbilityMod> abilityMods) {
            this.abilityMods = abilityMods;
            return this;
        }

        public Ancestry.Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Ancestry.Builder addLanguages(Language... language) {
            this.languages.addAll(Arrays.asList(language));
            return this;
        }

        public Ancestry.Builder addBonusLanguages(Language... language) {
            this.bonusLanguages.addAll(Arrays.asList(language));
            return this;
        }

        public Ancestry.Builder setBonusLanguages(List<Language> bonusLanguages) {
            this.bonusLanguages = bonusLanguages;
            return this;
        }
    }
}
