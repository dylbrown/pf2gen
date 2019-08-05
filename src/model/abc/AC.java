package model.abc;

import model.player.PC;
import model.abilities.Ability;
import model.ability_scores.AbilityMod;

import java.util.*;

public abstract class AC extends ABC {
    private final int HP;
    private final Map<Integer, List<Ability>> feats = new HashMap<>();
    private final Map<String, Ability> searchForFeats = new HashMap<>();

    AC(String name, String description, List<AbilityMod> abilityMods, int HP, List<Ability> feats) {
        super(name, description, abilityMods);
        this.HP = HP;
        for (Ability feat : feats) {
            for(int i = PC.MAX_LEVEL; i>=feat.getLevel(); i--) {
                this.feats.computeIfAbsent(i, (key)->new ArrayList<>()).add(feat);
            }
            searchForFeats.put(feat.toString().toLowerCase().trim(), feat);
        }

    }

    public int getHP() {
        return HP;
    }

    public List<Ability> getFeats(int level) {
        return Collections.unmodifiableList(feats.get(level));
    }

    public Ability findFeat(String contents) {
        return searchForFeats.get(contents.toLowerCase().trim());
    }
}
