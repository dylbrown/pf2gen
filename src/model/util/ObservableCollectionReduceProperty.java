package model.util;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import java.util.function.BinaryOperator;
import java.util.function.Function;

public class ObservableCollectionReduceProperty<V> implements ReadOnlyProperty<V> {
    private final Object bean;
    private final String name;
    private final ObjectProperty<V> property = new SimpleObjectProperty<>();

    public static <T> ObservableCollectionReduceProperty<Integer> makeIntegerSumProperty(ObservableMap<T, Integer> map, String name) {
        return new ObservableCollectionReduceProperty<>(map, Function.identity(), Integer::sum, (a, b) -> a - b, name, 0);
    }

    public <T, U> ObservableCollectionReduceProperty(ObservableMap<T, U> map, Function<U, V> transformer, BinaryOperator<V> merge, BinaryOperator<V> unmerge, String name, V identity) {
        bean = map;
        this.name = name;

        V result = map.values().stream().map(transformer).reduce(identity, merge);
        property.setValue(result);

        map.addListener((MapChangeListener<T, U>) change -> {
            V value;
            if(change.wasRemoved()) {
                if(unmerge != null) {
                    V base = (change.wasAdded()) ? transformer.apply(change.getValueAdded()) : identity;
                    value = unmerge.apply(
                            base,
                            transformer.apply(change.getValueRemoved()));
                } else {
                    property.setValue(map.values().stream().map(transformer).reduce(identity, merge));
                    return;
                }
            }else {
                value = transformer.apply(change.getValueAdded());
            }
            property.setValue(merge.apply(property.getValue(), value));
        });
    }

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addListener(ChangeListener<? super V> changeListener) {
        property.addListener(changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super V> changeListener) {
        property.removeListener(changeListener);
    }

    @Override
    public V getValue() {
        return property.getValue();
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        property.addListener(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        property.removeListener(invalidationListener);
    }
}
