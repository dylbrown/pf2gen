package ui.ftl;

import freemarker.template.*;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.SortedList;
import model.enums.Slot;
import model.equipment.Item;
import model.equipment.ItemCount;
import model.util.Pair;
import ui.ftl.wrap.ItemCountWrapper;

import java.util.Comparator;
import java.util.Map;

public class EquipmentList implements TemplateSequenceModel {
    private final ObservableList<Pair<Slot, ItemCount>> equipList = FXCollections.observableArrayList();
    private final ObservableList<ItemCount> unequipList = FXCollections.observableArrayList();
    private final SortedList<Pair<Slot, ItemCount>> equip = new SortedList<>(equipList,
            (Comparator.comparing(o -> o.second.stats())));
    private final SortedList<ItemCount> unequip = new SortedList<>(unequipList, Comparator.comparing(ItemCount::stats));
    private final ObjectWrapper wrapper;

    public EquipmentList(ObservableMap<Item, ItemCount> unequipped, ObservableMap<Slot, ItemCount> equipped, ObjectWrapper wrapper) {
        this.wrapper = wrapper;
        for (Map.Entry<Slot, ItemCount> entry : equipped.entrySet()) {
            equipList.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        unequipList.addAll(unequipped.values());
        equipped.addListener((MapChangeListener<Slot, ItemCount>) change -> {
            if(change.wasAdded()) {
                equipList.add(new Pair<>(change.getKey(), change.getValueAdded()));
            }
            if(change.wasRemoved()) {
                equipList.remove(findSourceIndex(new Pair<>(change.getKey(), change.getValueRemoved()),
                        equip, 0, equip.size()));
            }
        });
        unequipped.addListener((MapChangeListener<Item, ItemCount>) change -> {
            if(change.wasAdded()) {
                unequipList.add(change.getValueAdded());
            }
            if(change.wasRemoved()) {
                unequipList.remove(findSourceIndex(change.getValueRemoved(), unequip, 0, unequip.size()));
            }
        });
    }

    @Override
    public TemplateModel get(int i) {
        if(i > equipList.size() + unequipList.size()) throw new ArrayIndexOutOfBoundsException();
        if(i < equipList.size()) {
            return new ItemCountWrapper(equipList.get(i), wrapper);
        }else{
            return new ItemCountWrapper(unequipList.get(i - equipList.size()), wrapper);
        }
    }

    @Override
    public int size() {
        return equipList.size() + unequipList.size();
    }


    private int findSourceIndex(ItemCount itemCount, SortedList<ItemCount> list, int start, int end) {
        if(start > end) return -1;
        ItemCount mid = unequip.get((start + end) / 2);
        int compare = list.getComparator().compare(itemCount, mid);
        if(compare < 0) return findSourceIndex(itemCount, list, start, (start + end) / 2);
        if(compare > 0) return findSourceIndex(itemCount, list, (start + end) / 2 + 1, end);
        return list.getSourceIndex((start + end) / 2);
    }

    private int findSourceIndex(Pair<Slot, ItemCount> pair, SortedList<Pair<Slot, ItemCount>> list, int start, int end) {
        if(start > end) return -1;
        Pair<Slot, ItemCount> mid = equip.get((start + end) / 2);
        int compare = list.getComparator().compare(pair, mid);
        if(compare < 0) return findSourceIndex(pair, list, start, (start + end) / 2);
        if(compare > 0) return findSourceIndex(pair, list, (start + end) / 2 + 1, end);
        return list.getSourceIndex((start + end) / 2);
    }
}
