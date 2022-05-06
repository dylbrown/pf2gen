package model.abc;

import model.abilities.Ability;
import model.data_managers.sources.Source;
import model.player.PC;

import java.util.*;

public abstract class AC extends ABC {
    private final int HP;
    private final Map<Integer, List<Ability>> feats;

    AC(AC.Builder builder) {
        super(builder);
        this.HP = builder.HP;
        this.feats = builder.feats;
    }

    public int getHP() {
        return HP;
    }

    public List<Ability> getFeats(int level) {
        if(feats.get(level) == null) {
            System.out.println("Warning: Null feats at level "+level);
            return null;
        }
        return Collections.unmodifiableList(feats.get(level));
    }

    public static abstract class Builder extends ABC.Builder {
        private int HP = 0;
        private Map<Integer, List<Ability>> feats = Collections.emptyMap();

        protected Builder(Source source) {
            super(source);
        }

        public void setHP(int HP) {
            this.HP = HP;
        }

        public void setFeats(List<Ability> feats) {
            for (Ability feat : feats) {
                addFeat(feat);
            }
        }

        public void addFeat(Ability feat) {
            if(feats.size() == 0) {
                feats = new HashMap<>();
            }
            for(int i = PC.MAX_LEVEL; i>=feat.getLevel(); i--) {
                this.feats.computeIfAbsent(i, (key)->new ArrayList<>()).add(feat);
            }
        }
    }
}
