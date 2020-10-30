package model.equipment.weapons;

import model.enums.WeaponProficiency;
import model.equipment.Item;
import model.util.BiFunction;

public interface WeaponProficiencyTranslator extends BiFunction<Item, WeaponProficiency, WeaponProficiency> {
}
