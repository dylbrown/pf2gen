package model.equipment.armor;

import model.equipment.runes.Rune;

import java.util.HashMap;
import java.util.Map;

class RunedArmor extends Armor {
    private Map<String, Rune> runes = new HashMap<>();
    public RunedArmor(Armor armor) {
        super(new Armor.Builder(armor));
    }
}
