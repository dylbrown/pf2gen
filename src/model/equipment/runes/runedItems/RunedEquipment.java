package model.equipment.runes.runedItems;

import model.equipment.runes.Rune;

public interface RunedEquipment<R extends Rune> {
    Runes<R> getRunes();
}