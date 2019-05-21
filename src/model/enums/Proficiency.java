package model.enums;

public enum Proficiency {
    Untrained(-4), Trained(0), Expert(1), Master(2), Legendary(3);

    private final int mod;

    Proficiency(int modv) {
        mod = modv;
    }

    public int getMod() {
        return mod;
    }
}
