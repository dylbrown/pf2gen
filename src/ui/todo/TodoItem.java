package ui.todo;

import javafx.beans.value.ObservableBooleanValue;

public interface TodoItem extends Comparable<TodoItem> {
    boolean isFinished();
    ObservableBooleanValue finishedProperty();
    Priority getPriority();
    void navigateTo();
    default int compareTo(TodoItem i) {
        return getPriority().compareTo(i.getPriority());
    }
}
