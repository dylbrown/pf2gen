package model.util;

import javafx.beans.property.SimpleObjectProperty;

import java.util.Objects;

public class OPair<T, U> {
    public final SimpleObjectProperty<T> first;
    public final SimpleObjectProperty<U> second;

    public OPair(T t, U u) {
        this.first = new SimpleObjectProperty<>(t);
        this.second = new SimpleObjectProperty<>(u);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OPair<?, ?> oPair = (OPair<?, ?>) o;
        return Objects.equals(first.get(), oPair.first.get()) &&
                Objects.equals(second.get(), oPair.second.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(first.get(), second.get());
    }
}
