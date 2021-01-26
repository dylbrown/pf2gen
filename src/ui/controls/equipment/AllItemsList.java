package ui.controls.equipment;

import javafx.scene.control.TreeItem;
import model.items.Item;
import model.player.PC;
import ui.controls.lists.AbstractEntryList;
import ui.controls.lists.entries.ItemEntry;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AllItemsList<T extends Comparable<T>, U extends Comparable<U>> extends AbstractEntryList<Item, ItemEntry> {
    private final PC character;
    private final Function<Item, T> getFirstSeparator;
    private final Function<Item, U> getSecondSeparator;
    private final Function<Item, Item> transformer;

    public AllItemsList(PC character, BiConsumer<Item, Integer> handler, Function<Item, T> getFirstSeparator, Function<Item, U> getSecondSeparator) {
        this(character, handler, getFirstSeparator, getSecondSeparator, null);
    }

    public AllItemsList(PC character, BiConsumer<Item, Integer> handler, Function<Item, T> getFirstSeparator, Function<Item, U> getSecondSeparator, Function<Item, Item> transformer) {
        super();
        this.character = character;
        this.getFirstSeparator = getFirstSeparator;
        this.getSecondSeparator = getSecondSeparator;
        this.transformer = transformer;
        construct(handler);

    }

    @Override
    protected void addItems(TreeItem<ItemEntry> root) {
        Map<T, Map<U, TreeItem<ItemEntry>>> cats = new TreeMap<>();
        for (Item item : character.sources().equipment().getAll().values()) {
            addItem(cats, item);
        }
        for (Item item : character.sources().armor().getAll().values()) {
            addItem(cats, item);
        }
        for (Item item : character.sources().weapons().getAll().values()) {
            addItem(cats, item);
        }
        for (Map.Entry<T, Map<U, TreeItem<ItemEntry>>> entry : cats.entrySet()) {
            TreeItem<ItemEntry> firstDivider = new TreeItem<>(new ItemEntry(String.valueOf(entry.getKey())));
            root.getChildren().add(firstDivider);
            if(entry.getValue().size() > 1)
                firstDivider.getChildren().addAll(entry.getValue().values());
            else {
                firstDivider.getChildren().addAll(entry.getValue().values().iterator().next().getChildren());
            }
        }
    }

    protected void addItem(Map<T, Map<U, TreeItem<ItemEntry>>> cats, Item item) {
        if(transformer != null)
            item = transformer.apply(item);
        T firstSeparator = getFirstSeparator.apply(item);
        cats.computeIfAbsent(firstSeparator, (s)->new TreeMap<>())
                .computeIfAbsent(getSecondSeparator.apply(item), (s)->new TreeItem<>(new ItemEntry(s.toString())))
                .getChildren().add(new TreeItem<>(new ItemEntry(item)));
    }
}
