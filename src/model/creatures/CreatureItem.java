package model.creatures;

import model.equipment.Item;

public class CreatureItem {
    private final String itemName;
    private final Item item;

    public CreatureItem(Item item) {
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

    public Item getItem() {
        return item;
    }
}
