package model.abc;

import model.abilities.Ability;
import model.enums.Language;
import model.enums.Sense;
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
    private final List<Ability> grantedAbilities;
    private final List<Language> languages;
    private final List<Language> bonusLanguages;
    private final List<Sense> senses;

    private Ancestry(Ancestry.Builder builder) {
        super(builder);
        this.size = builder.size;
        this.speed = builder.speed;
        this.heritages = builder.heritages;
        this.grantedAbilities = builder.grantedAbilities;
        this.languages = builder.languages;
        this.bonusLanguages = builder.bonusLanguages;
        this.bonusLanguages.removeIf(language -> language.getName().equals("Free"));
        this.senses = builder.senses;
    }

    public List<Ability> getHeritages() {
        return Collections.unmodifiableList(heritages);
    }

    public List<Ability> getGrantedAbilities() {
        return grantedAbilities;
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

    public List<Sense> getSenses() {
        return Collections.unmodifiableList(senses);
    }

    public static class Builder extends AC.Builder {
        private Size size = Size.Medium;
        private int speed = 0;
        private final List<Ability> heritages = new ArrayList<>();
        private List<Ability> grantedAbilities = Collections.emptyList();
        private final List<Language> languages = new ArrayList<>();
        private final List<Language> bonusLanguages = new ArrayList<>();
        private final List<Sense> senses = new ArrayList<>();

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

        public void addBonusLanguages(Collection<Language> language) {
            this.bonusLanguages.addAll(language);
        }

        public void addBonusLanguages(Language... language) {
            this.addBonusLanguages(Arrays.asList(language));
        }

        public void addSenses(Sense... sense) {
            senses.addAll(Arrays.asList(sense));
        }

        public void addGrantedAbility(Ability ability) {
            if(grantedAbilities.isEmpty()) grantedAbilities = new ArrayList<>();
            grantedAbilities.add(ability);
        }

        public Ancestry build() {
            return new Ancestry(this);
        }
    }
}
