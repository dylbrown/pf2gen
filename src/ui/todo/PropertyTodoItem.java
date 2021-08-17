package ui.todo;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableBooleanValue;

public class PropertyTodoItem<T> extends AbstractTodoItem {

    private final ReadOnlyObjectProperty<T> property;
    private final ObservableBooleanValue finished;

    public PropertyTodoItem(String name, ReadOnlyObjectProperty<T> property, Priority priority, Runnable navigateTo) {
        super(name, priority, navigateTo);
        this.property = property;
        this.finished = property.isNotNull();
    }
    @Override
    public boolean isFinished() {
        return property.isNotNull().get();
    }

    @Override
    public ObservableBooleanValue finishedProperty() {
        return this.finished;
    }
}
