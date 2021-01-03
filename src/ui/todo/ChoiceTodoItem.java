package ui.todo;

import javafx.beans.value.ObservableBooleanValue;
import model.ability_slots.Choice;

public class ChoiceTodoItem<T> extends AbstractTodoItem {
    private final Choice<T> choice;

    public ChoiceTodoItem(Choice<T> choice, Priority priority, Runnable navigateTo) {
        super(choice.getName(), priority, navigateTo);
        priority.append(choice.getLevel());
        this.choice = choice;
    }

    @Override
    public boolean isFinished() {
        return choice.getSelections().size() == choice.getMaxSelections();
    }

    @Override
    public ObservableBooleanValue finishedProperty() {
        return choice.numSelectionsProperty().greaterThanOrEqualTo(choice.maxSelectionsProperty());
    }
}
