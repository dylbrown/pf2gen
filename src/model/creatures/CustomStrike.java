package model.creatures;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import model.creatures.scaling.ScaleMap;
import model.enums.Trait;

import java.util.List;

public class CustomStrike implements Attack {
    public final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper("");
    public final CustomCreatureValue<String> modifier;
    public List<Trait> traits;
    public final ReadOnlyStringWrapper damage = new ReadOnlyStringWrapper("");
    public AttackType attackType;

    public CustomStrike(IntegerProperty level) {
        modifier = new CustomCreatureValue<>("Attack", level, ScaleMap.ATTACK_BONUS_SCALES);
    }

    public String getName() {
        return name.get();
    }

    public int getModifier() {
        return modifier.getModifier();
    }

    public List<Trait> getTraits() {
        return traits;
    }

    public String getDamage() {
        return damage.get();
    }

    public AttackType getAttackType() {
        return attackType;
    }
}
