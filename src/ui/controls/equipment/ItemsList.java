package ui.controls.equipment;

import javafx.collections.ObservableList;
import model.items.Item;
import ui.controls.lists.ObservableCategoryEntryList;
import ui.controls.lists.entries.ItemEntry;

import java.util.function.BiConsumer;

public class ItemsList extends ObservableCategoryEntryList<Item, ItemEntry> {
    public ItemsList(ObservableList<Item> items, BiConsumer<Item, Integer> handler) {
        super(items, handler,
                Item::getCategory,
                Item::getSubCategory,
                ItemEntry::new, ItemEntry::new,
                CategoryAllItemsList::makeColumns);
    }
}
