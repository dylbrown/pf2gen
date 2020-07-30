package model.util;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

import java.util.function.Function;

public class WrapperTransformationList<E, F> extends TransformationList<E, F> {

    private final Function<F, E> wrapper;

    public WrapperTransformationList(ObservableList<F> list, Function<F, E> wrapper) {
        super(list);
        this.wrapper = wrapper;
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends F> change) {

    }

    @Override
    public int getSourceIndex(int i) {
        return i;
    }

    @Override
    public int getViewIndex(int i) {
        return i;
    }

    @Override
    public E get(int index) {
        return wrapper.apply(getSource().get(index));
    }

    @Override
    public int size() {
        return getSource().size();
    }
}
