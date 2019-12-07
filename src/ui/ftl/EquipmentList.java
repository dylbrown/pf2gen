package ui.ftl;

import freemarker.template.Configuration;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.SortedList;
import model.enums.Slot;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.util.Pair;
import ui.ftl.entries.ItemCountWrapper;

import java.util.Comparator;
import java.util.Map;

public class EquipmentList implements TemplateSequenceModel {
    private ObservableList<Pair<Slot, ItemCount>> equipList = FXCollections.observableArrayList();
    private ObservableList<ItemCount> unequipList = FXCollections.observableArrayList();
    private SortedList<Pair<Slot, ItemCount>> equip = new SortedList<>(equipList,
            (Comparator.comparing(o -> o.second.stats())));
    private SortedList<ItemCount> unequip = new SortedList<>(unequipList, Comparator.comparing(ItemCount::stats));

    EquipmentList(ObservableMap<Equipment, ItemCount> unequipped, ObservableMap<Slot, ItemCount> equipped) {
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
        unequipped.addListener((MapChangeListener<Equipment, ItemCount>) change -> {
            if(change.wasAdded()) {
                unequipList.add(change.getValueAdded());
            }
            if(change.wasRemoved()) {
                unequipList.remove(findSourceIndex(change.getValueRemoved(), unequip, 0, unequip.size()));
            }
        });
    }

    @Override
    public TemplateModel get(int i) throws TemplateModelException {
        if(i > equipList.size() + unequipList.size()) throw new ArrayIndexOutOfBoundsException();
        ItemCountWrapper wrapper;
        if(i < equipList.size()) {
            wrapper = new ItemCountWrapper(equipList.get(i));
        }else{
            wrapper = new ItemCountWrapper(unequipList.get(i - equipList.size()));
        }
        return Configuration.getDefaultObjectWrapper(Configuration.getVersion()).wrap(wrapper);
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
