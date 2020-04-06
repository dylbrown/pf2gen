package model.equipment.weapons;

import java.util.ArrayList;
import java.util.List;

public class Damage {
    public static final Damage ZERO = new Damage(Dice.get(0,0), 0, null);
    private final Dice dice;
    private final int amount;
    private final DamageType damageType;

    public Damage(Dice dice, int modifier, DamageType damageType) {
        this.dice = dice;
        this.amount = modifier;
        this.damageType = damageType;
    }

    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        if(dice.getCount() > 0 && dice.getSize() > 0) parts.add(dice.toString());
        if(amount != 0) parts.add(String.valueOf(amount));
        if(damageType == null) return String.join(" + ", parts);
        return String.join(" + ", parts) + " " + damageType;
    }
}
