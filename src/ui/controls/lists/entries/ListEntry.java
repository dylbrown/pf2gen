package ui.controls.lists.entries;

import com.sun.javafx.fxml.PropertyNotFoundException;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import model.util.StringUtils;

public class ListEntry<T> implements Comparable<ListEntry<T>>, TreeTableEntry {
    private final T contents;
    private final ObservableValue<String> name;

    public ListEntry(T item, ObservableValue<String> name) {
        this.contents = item;
        this.name = name;
    }

    public ListEntry(T item, String name) {
        this(item, new ReadOnlyStringWrapper(name).getReadOnlyProperty());
    }

    public ListEntry(T item) {
        this(item, item.toString());
    }

    public ListEntry(String label) {
        this(null, new ReadOnlyStringWrapper(StringUtils.unclean(label)).getReadOnlyProperty());
    }

    protected ObservableValue<String> nameProperty() {
        return name;
    }

    public T getContents() {
        return contents;
    }

    @Override
    public int compareTo(ListEntry<T> o) {
        if(this.getContents() == null && o.getContents() != null) return 1;
        if(this.getContents() != null && o.getContents() == null) return -1;
        return StringUtils.clean(this.toString()).compareTo(StringUtils.clean(o.toString()));
    }

    @Override
    public String toString() {
        if(contents == null) return this.name.getValue();
        else return contents.toString();
    }

    @Override
    public ObservableValue<String> get(String propertyName) {
        if ("name".equals(propertyName)) {
            return nameProperty();
        }
        throw new PropertyNotFoundException();
    }
}

