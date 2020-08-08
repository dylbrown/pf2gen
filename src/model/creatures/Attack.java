package model.creatures;

import model.equipment.CustomTrait;

import java.util.Collections;
import java.util.List;

public class Attack {
    private final String name;
    private final int modifier;
    private final List<CustomTrait> traits;
    private final String damage;
    private final AttackType attackType;

    private Attack(Builder builder) {
        name = builder.name;
        modifier = builder.modifier;
        traits = Collections.unmodifiableList(builder.traits);
        damage = builder.damage;
        attackType = builder.attackType;
    }

    public String getName() {
        return name;
    }

    public int getModifier() {
        return modifier;
    }

    public List<CustomTrait> getTraits() {
        return traits;
    }

    public String getDamage() {
        return damage;
    }

    public AttackType getAttackType() {
        return attackType;
    }

    public static class Builder {
        private String name = "";
        private int modifier = 0;
        private List<CustomTrait> traits = Collections.emptyList();
        private String damage = "";
        private AttackType attackType;

        public void setName(String name) {
            this.name = name;
        }

        public void setModifier(int modifier) {
            this.modifier = modifier;
        }

        public void setTraits(List<CustomTrait> traits) {
            this.traits = traits;
        }

        public void setDamage(String damage) {
            this.damage = damage;
        }

        public void setAttackType(AttackType type) {
            this.attackType = type;
        }

        public Attack build() {
            return new Attack(this);
        }
    }
}
