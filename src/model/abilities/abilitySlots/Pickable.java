package model.abilities.abilitySlots;

import model.abilities.Ability;

import java.util.List;

public interface Pickable {
    List<Ability> getAbilities(int level);
    void fill(Ability choice);
}
