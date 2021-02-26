package ui.todo;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableBooleanValue;

public class PropertyTodoItem<T> extends AbstractTodoItem {

    private final ReadOnlyObjectProperty<T> property;

    public PropertyTodoItem(String name, ReadOnlyObjectProperty<T> property, Priority priority, Runnable navigateTo) {
        super(name, priority, navigateTo);
        this.property = property;
    }
    @Override
    public boolean isFinished() {
        return property.isNotNull().get();
    }

    @Override
    public ObservableBooleanValue finishedProperty() {
        return property.isNotNull();
    }
}
