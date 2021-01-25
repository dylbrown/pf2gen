package model.util;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;

import java.util.function.Predicate;

public class PropertyPredicate<T> implements ObservableBooleanValue {

    private SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty();

    public PropertyPredicate(ReadOnlyObjectProperty<T> property, Predicate<T> predicate) {
        booleanProperty.set(predicate.test(property.getValue()));
        property.addListener((o, oldVal, newVal) -> booleanProperty.set(predicate.test(newVal)));
    }

    @Override
    public boolean get() {
        return booleanProperty.get();
    }

    @Override
    public void addListener(ChangeListener<? super Boolean> changeListener) {
        booleanProperty.addListener(changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super Boolean> changeListener) {
        booleanProperty.removeListener(changeListener);

    }

    @Override
    public Boolean getValue() {
        return booleanProperty.get();
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        booleanProperty.addListener(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        booleanProperty.removeListener(invalidationListener);
    }
}
