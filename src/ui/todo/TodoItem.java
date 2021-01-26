package ui.todo;

import javafx.beans.value.ObservableValue;

public interface TodoItem extends Comparable<TodoItem> {
    boolean isFinished();
    ObservableValue<Boolean> finishedProperty();
    Priority getPriority();
    void navigateTo();
    default int compareTo(TodoItem i) {
        return getPriority().compareTo(i.getPriority());
    }
}
