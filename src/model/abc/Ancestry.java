package model.abc;

import model.abilities.Ability;
import model.enums.Language;
import model.enums.Size;

import java.util.*;

public class Ancestry extends AC {
    public static final Ancestry NO_ANCESTRY;
    static{
        Builder builder = new Builder();
        builder.setName("No Ancestry");
        NO_ANCESTRY = builder.build();
    }
    private final Size size;
    private final int speed;
    private final List<Ability> heritages;
    private final List<Language> languages;
    private final List<Language> bonusLanguages;
    private final List<String> senses;

    private Ancestry(Ancestry.Builder builder) {
        super(builder);
        this.size = builder.size;
        this.speed = builder.speed;
        this.heritages = builder.heritages;
        this.languages = builder.languages;
        this.bonusLanguages = builder.bonusLanguages;
        this.senses = builder.senses;
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

    public List<String> getSenses() {
        return Collections.unmodifiableList(senses);
    }

    public static class Builder extends AC.Builder {
        private Size size = Size.Medium;
        private int speed = 0;
        private final List<Ability> heritages = new ArrayList<>();
        private final List<Language> languages = new ArrayList<>();
        private final List<Language> bonusLanguages = new ArrayList<>();
        private final List<String> senses = new ArrayList<>();

        public Ancestry build() {
            return new Ancestry(this);
        }

        public void setSize(Size size) {
            this.size = size;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public void addHeritage(Ability heritage) {
            this.heritages.add(heritage);
        }

        public void addLanguages(Language... language) {
            this.languages.addAll(Arrays.asList(language));
            this.bonusLanguages.removeAll(Arrays.asList(language));
        }

        public void addBonusLanguages(Language... language) {
            this.bonusLanguages.addAll(Arrays.asList(language));
        }

        public void addSenses(String... sense) {
            senses.addAll(Arrays.asList(sense));
        }
    }
}
