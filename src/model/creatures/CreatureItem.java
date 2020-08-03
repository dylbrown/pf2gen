package model.creatures;

import model.equipment.Equipment;

public class CreatureItem {
    private final String itemName;
    private final Equipment item;

    public CreatureItem(Equipment item) {
        this.itemName = item.getName();
        this.item = item;
    }

    public CreatureItem(String itemName) {
        this.itemName = itemName;
        this.item = null;
    }

    public String getItemName() {
        return itemName;
    }

    public Equipment getItem() {
        return item;
    }
}
