package model.abc;

import model.abilities.Ability;
import model.ability_scores.AbilityMod;
import model.enums.Language;
import model.enums.Size;

import java.util.Collections;
import java.util.List;

public class Ancestry extends AC {
    public static final Ancestry NO_ANCESTRY = new Ancestry("No Ancestry", "", Collections.emptyList(), Collections.emptyList(), 0, Size.Medium, 0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    private final Size size;
    private final int speed;
    private final List<Ability> heritages;
    private final List<Language> languages;
    private final List<Language> bonusLanguages;

    public Ancestry(String name, String description, List<Language> languages, List<Language> bonusLanguages, int HP, Size size, int speed, List<AbilityMod> abilityMods, List<Ability> feats, List<Ability> heritages){
        super(name, description, abilityMods, HP, feats);
        this.size = size;
        this.speed = speed;
        this.heritages = heritages;
        this.languages = languages;
        this.bonusLanguages = bonusLanguages;
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
}
