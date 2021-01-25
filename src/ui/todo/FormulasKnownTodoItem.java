package ui.todo;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableBooleanValue;
import model.player.PC;
import model.util.ObservableCollectionReduceProperty;

public class FormulasKnownTodoItem extends AbstractTodoItem {
    private final ObservableCollectionReduceProperty<Integer> formulas_remaining;
    private final ReadOnlyBooleanWrapper booleanWrapper;
    private final ReadOnlyBooleanProperty readOnly;

    public FormulasKnownTodoItem(PC character, Priority priority, Runnable navigateTo) {
        super("Formulas Remaining", priority, navigateTo);
        formulas_remaining = ObservableCollectionReduceProperty.makeIntegerSumProperty(
                FormulasKnownTracker.getFormulasRemainingTracker(character),
                "Formulas Remaining");
        booleanWrapper = new ReadOnlyBooleanWrapper(formulas_remaining.getValue() <= 0);
        readOnly = booleanWrapper.getReadOnlyProperty();

        formulas_remaining.addListener(
                (o, oldVal, newVal) -> booleanWrapper.set(newVal <= 0));
    }

    @Override
    public boolean isFinished() {
        return formulas_remaining.getValue() <= 0;
    }

    @Override
    public ObservableBooleanValue finishedProperty() {
        return readOnly;
    }
}
