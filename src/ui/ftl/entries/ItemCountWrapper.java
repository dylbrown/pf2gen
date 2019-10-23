package ui.ftl.entries;

import model.enums.Slot;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.util.Pair;

public class ItemCountWrapper {
    private ItemCount itemCount;
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
}
