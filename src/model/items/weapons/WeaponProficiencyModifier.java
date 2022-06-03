package model.items.weapons;

import model.enums.Proficiency;
import model.items.Item;
import model.util.BiFunction;

public interface WeaponProficiencyModifier extends BiFunction<Item, Proficiency, Proficiency> {
}
