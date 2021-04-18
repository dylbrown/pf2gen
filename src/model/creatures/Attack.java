package model.creatures;

import model.enums.Trait;

import java.util.List;

public interface Attack {
    String getName();

    int getModifier();

    List<Trait> getTraits();

    String getDamage();

    AttackType getAttackType();
}