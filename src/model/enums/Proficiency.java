package model.enums;

import model.util.StringUtils;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public enum Proficiency {
    Untrained(0), Trained(2), Expert(4), Master(6), Legendary(8);

    private final int mod;

    Proficiency(int mod) {
        this.mod = mod;
    }

    public static Proficiency max(Proficiency... profs) {
        Optional<Proficiency> max = Stream.of(profs).max(Comparator.comparingInt(Proficiency::getMod));
        return max.orElse(Proficiency.Untrained);
    }

    private static final Proficiency[] notUntrained = new Proficiency[]
            {Proficiency.Trained, Proficiency.Expert, Proficiency.Master, Proficiency.Legendary};

    public static Proficiency[] notUntrained() {
        return notUntrained;
    }

    public static Proficiency robustValueOf(String proficiency) {
        return valueOf(StringUtils.camelCaseWord(proficiency.trim()));
    }

    public int getMod(){
        return mod;
    }

    public int getMod(int level){
        if(this == Untrained)
            return mod;
        return level + mod;
    }

    public static int getMinLevel(Proficiency proficiency) {
        switch(proficiency){
            case Untrained:
            case Trained:
            default: return 1;
            case Expert: return 2;
            case Master: return 7;
            case Legendary: return 15;
        }
    }

    public int getMinLevel() {
        return getMinLevel(this);
    }
}
