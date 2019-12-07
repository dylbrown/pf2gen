package ui.ftl.entries;

import model.enums.Slot;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.equipment.Weapon;
import model.util.Pair;
import ui.Main;

public class ItemCountWrapper {
    private final ItemCount itemCount;
    private Slot slot = null;
    public ItemCountWrapper(ItemCount itemCount) {
        this.itemCount = itemCount;
    }

    public ItemCountWrapper(Pair<Slot, ItemCount> pair) {
        this.slot = pair.first;
        this.itemCount = pair.second;
    }

    public Equipment getStats() {
        return itemCount.stats();
    }

    public String getLocation() {
        if(slot != null) {
            return Slot.getPrettyName(slot);
        }else{
            return "Carried";
        }
    }

    public int getCount() {
        return itemCount.getCount();
    }

    public int getAttack() {
        return Main.character.getAttackMod((Weapon) itemCount.stats());
    }

    public int getDamagemod() {
        return Main.character.getDamageMod((Weapon) itemCount.stats());
    }
}
