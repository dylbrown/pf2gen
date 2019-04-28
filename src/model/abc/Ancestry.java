package model.abc;

import model.abilities.Ability;
import model.abilityScores.AbilityMod;
import model.enums.Size;

import java.util.Collections;
import java.util.List;

public class Ancestry extends ABC {
   // Gnome(8, Small, 20), Goblin(6, Small, 25), Halfling(6, Small, 25), Human(8, Medium, 25);

    private final int HP;
    private final Size size;
    private final int speed;
    private final List<Ability> feats;

    public Ancestry(String name, int HP, Size size, int speed, List<AbilityMod> abilityMods, List<Ability> feats){
        super(name, abilityMods);
        this.HP = HP;
        this.size = size;
        this.speed = speed;
        this.feats = feats;
        //TODO: Add languages
    }

    public int getHP() {
        return HP;
    }

    public List<Ability> getFeats() {
        return feats;
    }

    public int getSpeed() {
        return speed;
    }
}
