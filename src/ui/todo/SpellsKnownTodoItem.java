package ui.todo;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.spells.Spell;
import model.spells.SpellList;

public class SpellsKnownTodoItem extends AbstractTodoItem {
    private final SpellList list;
    private final int level;
    private final ReadOnlyBooleanWrapper finished = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyBooleanProperty finishedReadOnly = finished.getReadOnlyProperty();

    public SpellsKnownTodoItem(SpellList list, int level, Priority priority, Runnable navigateTo) {
        super(list.getIndex() + level, priority, navigateTo);
        priority.append(level);
        this.list = list;
        this.level = level;
        list.getSpellSlots().addListener((ListChangeListener<Integer>) c->
                finished.set(isFinished()));
        list.getSpellsKnown().addListener((ListChangeListener<Integer>) c->
                finished.set(isFinished()));
        list.getSpellsKnown(level).addListener((ListChangeListener<Spell>) c->
                finished.set(isFinished()));
        finished.set(isFinished());
    }

    @Override
    public boolean isFinished() {
        ObservableList<Integer> extraSpellsKnown = list.getSpellsKnown();
        int numSlots = (level >= extraSpellsKnown.size()) ? 0 : extraSpellsKnown.get(level);
        return list.getSpellsKnown(level).size() >= numSlots;
    }

    @Override
    public ObservableBooleanValue finishedProperty() {
        return finishedReadOnly;
    }

    @Override
    public String toString() {
        return "Select Level "+level+" "+list.getIndex()+" Spells Known";
    }
}
