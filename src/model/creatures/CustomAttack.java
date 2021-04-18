package model.creatures;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import model.enums.Trait;

import java.util.List;

public class CustomAttack implements Attack {
    public final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper("");
    public final ReadOnlyIntegerWrapper modifier = new ReadOnlyIntegerWrapper(0);
    public List<Trait> traits;
    public final ReadOnlyStringWrapper damage = new ReadOnlyStringWrapper("");
    public AttackType attackType;

    public String getName() {
        return name.get();
    }

    public int getModifier() {
        return modifier.get();
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
