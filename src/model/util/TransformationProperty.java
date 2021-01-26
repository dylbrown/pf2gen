package model.util;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.function.Function;

public class TransformationProperty<T, U> implements ObservableValue<U> {

    private ObjectProperty<U> objectProperty = new SimpleObjectProperty<>();

    public TransformationProperty(ReadOnlyObjectProperty<T> property, Function<T, U> transform) {
        objectProperty.set(transform.apply(property.getValue()));
        property.addListener((o, oldVal, newVal) -> objectProperty.set(transform.apply(newVal)));
    }

    @Override
    public void addListener(ChangeListener<? super U> changeListener) {
        objectProperty.addListener(changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super U> changeListener) {
        objectProperty.removeListener(changeListener);

    }

    @Override
    public U getValue() {
        return objectProperty.get();
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        objectProperty.addListener(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        objectProperty.removeListener(invalidationListener);
    }
}
