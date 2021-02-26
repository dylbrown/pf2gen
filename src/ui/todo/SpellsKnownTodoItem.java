package ui.todo;

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

    public SpellsKnownTodoItem(SpellList list, int level, Priority priority, Runnable navigateTo) {
        super(list.getIndex() + level, priority, navigateTo);
        priority.append(level);
        this.list = list;
        this.level = level;
        list.getSpellSlots().addListener((ListChangeListener<Integer>) c->
                finished.set(isFinished()));
        list.getExtraSpellsKnown().addListener((ListChangeListener<Integer>) c->
                finished.set(isFinished()));
        list.getSpellsKnown(level).addListener((ListChangeListener<Spell>) c->
                finished.set(isFinished()));
        finished.set(isFinished());
    }

    @Override
    public boolean isFinished() {
        ObservableList<Integer> spellSlots = list.getSpellSlots();
        ObservableList<Integer> extraSpellsKnown = list.getExtraSpellsKnown();
        int numSlots = (level >= spellSlots.size()) ? 0 : spellSlots.get(level);
        numSlots += (level >= extraSpellsKnown.size()) ? 0 : extraSpellsKnown.get(level);
        return list.getSpellsKnown(level).size() >= numSlots;
    }

    @Override
    public ObservableBooleanValue finishedProperty() {
        return finished.getReadOnlyProperty();
    }

    @Override
    public String toString() {
        return "Select Level "+level+" "+list.getIndex()+" Spells Known";
    }
}
