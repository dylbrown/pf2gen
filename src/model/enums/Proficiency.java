package model.enums;

public enum Proficiency {
    Untrained(0), Trained(2), Expert(4), Master(6), Legendary(8);

    private final int mod;

    Proficiency(int mod) {
        this.mod = mod;
    }

    public static Proficiency max(Proficiency oldProf, Proficiency newProf) {
        if(oldProf.getMod(0) > newProf.getMod(0))
            return oldProf;
        return newProf;
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
}
