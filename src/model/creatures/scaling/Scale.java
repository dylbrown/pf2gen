package model.creatures.scaling;

public enum Scale {
    Terrible, Low, Moderate, High, Extreme;

    public static Scale getNearest(double position) {
        switch ((int) Math.round(position)) {
            case 0: return Terrible;
            case 1: return Low;
            case 2: return Moderate;
            case 3: return High;
            case 4: return Extreme;
        }
        return (position < 0) ? Terrible : Extreme;
    }
}
