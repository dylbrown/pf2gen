package ui.controls.lists.entries;

import javafx.beans.value.ObservableValue;

public interface TreeTableEntry {
    ObservableValue<String> get(String propertyName);
}
