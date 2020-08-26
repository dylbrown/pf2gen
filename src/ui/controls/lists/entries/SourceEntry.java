package ui.controls.lists.entries;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import model.data_managers.sources.Source;

public class SourceEntry extends ListEntry<Source> {
    private final ReadOnlyStringWrapper id = new ReadOnlyStringWrapper("");
    private final BooleanProperty enabled = new SimpleBooleanProperty(false);
    private final BooleanProperty indeterminate = new SimpleBooleanProperty(false);
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

    public Boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public Boolean isIndeterminate() {
        return indeterminate.get();
    }

    public BooleanProperty indeterminateProperty() {
        return indeterminate;
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
