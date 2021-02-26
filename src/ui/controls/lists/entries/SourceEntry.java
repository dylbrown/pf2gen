package ui.controls.lists.entries;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import model.data_managers.sources.Source;
import ui.controls.lists.ThreeState;

public class SourceEntry extends ListEntry<Source> {
    private final ReadOnlyStringWrapper id = new ReadOnlyStringWrapper("");
    private final ObjectProperty<ThreeState> state = new SimpleObjectProperty<>(ThreeState.False);
    private boolean locked = false;
    public SourceEntry(Source source) {
        super(source, source.getName());
        id.set(source.getShortName());
    }

    public SourceEntry(String label) {
        super(label);
    }

    public String getId() {
        return id.get();
    }

    public ReadOnlyStringWrapper idProperty() {
        return id;
    }

    public ThreeState getState() {
        return state.get();
    }

    public ObjectProperty<ThreeState> stateProperty() {
        return state;
    }

    public void lock() {
        locked = true;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public ObservableValue<String> get(String propertyName) {
        switch (propertyName) {
            case "name": return nameProperty();
            case "id": return idProperty();
            case "enabled": return state.asString();
        }
        return null;
    }
}
