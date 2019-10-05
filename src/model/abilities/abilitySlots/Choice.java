package model.abilities.abilitySlots;

import javafx.collections.ObservableList;

import java.util.List;

public interface Choice<T> {
    void add(T choice);
    void remove(T choice);
    void empty();
    int getNumSelections();
    ObservableList<T> getSelections();
    default List<T> viewSelections() {return getSelections(); }
    int getLevel();
}
