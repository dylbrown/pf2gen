package model.abc;

import model.abilities.Ability;
import model.abilityScores.AbilityMod;
import model.enums.Size;

import java.util.Collections;
import java.util.List;

public class Ancestry extends AC {
    private final Size size;
    private final int speed;
    private final List<Ability> heritages;

    public Ancestry(String name, String description, int HP, Size size, int speed, List<AbilityMod> abilityMods, List<Ability> feats, List<Ability> heritages){
        super(name, description, abilityMods, HP, feats);
        this.size = size;
        this.speed = speed;
        this.heritages = heritages;
        //TODO: Add languages
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
}
