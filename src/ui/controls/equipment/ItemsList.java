package ui.controls.equipment;

import javafx.collections.ObservableList;
import model.equipment.Equipment;
import ui.controls.lists.ObservableCategoryEntryList;
import ui.controls.lists.entries.ItemEntry;

import java.util.function.BiConsumer;

public class ItemsList extends ObservableCategoryEntryList<Equipment, ItemEntry> {
    public ItemsList(ObservableList<Equipment> items, BiConsumer<Equipment, Integer> handler) {
        super(items, handler,
                Equipment::getCategory,
                Equipment::getSubCategory,
                ItemEntry::new, ItemEntry::new,
                CategoryAllItemsList::makeColumns);
    }
}
