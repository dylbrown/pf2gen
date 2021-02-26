package model.items.weapons;

import model.enums.WeaponProficiency;
import model.items.Item;
import model.util.BiFunction;

public interface WeaponProficiencyTranslator extends BiFunction<Item, WeaponProficiency, WeaponProficiency> {
}
