package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import model.util.StringUtils;

public abstract class ListEntry<T> implements Comparable<ListEntry<T>>, TreeTableEntry {
    private final T contents;
    private final ReadOnlyStringProperty name;

    public ListEntry(T item, ReadOnlyStringProperty name) {
        this.contents = item;
        this.name = name;
    }

    public ListEntry(T item, String name) {
        this.contents = item;
        this.name = new ReadOnlyStringWrapper(name).getReadOnlyProperty();
    }

    public ListEntry(String label) {
        this.contents = null;
        this.name = new ReadOnlyStringWrapper(StringUtils.unclean(label)).getReadOnlyProperty();
    }

    protected ReadOnlyStringProperty nameProperty() {
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
        if(contents == null) return this.name.get();
        else return contents.toString();
    }
}

