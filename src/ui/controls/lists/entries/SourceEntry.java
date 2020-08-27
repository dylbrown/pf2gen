package ui.controls.lists.entries;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import model.data_managers.sources.Source;
import ui.controls.lists.ThreeState;

public class SourceEntry extends ListEntry<Source> {
    private final ReadOnlyStringWrapper id = new ReadOnlyStringWrapper("");
    private final ObjectProperty<ThreeState> enabled = new SimpleObjectProperty<>(ThreeState.False);
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

    public ThreeState getEnabled() {
        return enabled.get();
    }

    public ObjectProperty<ThreeState> enabledProperty() {
        return enabled;
    }

    @Override
    public ObservableValue<String> get(String propertyName) {
        switch (propertyName) {
            case "name": return nameProperty();
            case "id": return idProperty();
            case "enabled": return enabled.asString();
        }
        return null;
    }
}
