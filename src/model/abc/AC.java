package model.abc;

import model.abilities.Ability;
import model.player.PC;

import java.util.*;

public abstract class AC extends ABC {
    private final int HP;
    private final Map<Integer, List<Ability>> feats;
    private final Map<String, Ability> searchForFeats;

    AC(AC.Builder builder) {
        super(builder);
        this.HP = builder.HP;
        this.feats = builder.feats;
        this.searchForFeats = builder.searchForFeats;
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

    public static abstract class Builder extends ABC.Builder {
        private int HP = 0;
        private Map<Integer, List<Ability>> feats = Collections.emptyMap();
        private Map<String, Ability> searchForFeats = Collections.emptyMap();

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
                searchForFeats = new HashMap<>();
            }
            for(int i = PC.MAX_LEVEL; i>=feat.getLevel(); i--) {
                this.feats.computeIfAbsent(i, (key)->new ArrayList<>()).add(feat);
            }
            searchForFeats.put(feat.toString().toLowerCase().trim(), feat);
        }
    }
}
