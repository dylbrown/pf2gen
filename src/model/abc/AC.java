package model.abc;

import model.PC;
import model.abilities.Ability;
import model.abilityScores.AbilityMod;

import java.util.*;

public class AC extends ABC {
    protected final int HP;
    protected final Map<Integer, List<Ability>> feats = new HashMap<>();

    public AC(String name, String description, List<AbilityMod> abilityMods, int HP, List<Ability> feats) {
        super(name, description, abilityMods);
        this.HP = HP;
        for (Ability feat : feats) {
            for(int i = PC.MAX_LEVEL; i>=feat.getLevel(); i--) {
                this.feats.computeIfAbsent(i, (key)->new ArrayList<>()).add(feat);
            }
        }

    }

    public int getHP() {
        return HP;
    }

    public List<Ability> getFeats(int level) {
        return Collections.unmodifiableList(feats.get(level));
    }
}
