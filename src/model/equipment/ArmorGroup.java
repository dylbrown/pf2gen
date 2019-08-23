package model.equipment;

public class ArmorGroup {
    private final String effect;
    private final String name;
    public static final ArmorGroup None = new ArmorGroup("","—");

    public ArmorGroup(String effect, String name) {
        this.effect = effect;
        this.name = name;
    }

    public String getEffect() {
        return effect;
    }

    public String getName() {
        return name;
    }
}
