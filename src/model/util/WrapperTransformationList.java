package model.util;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

import java.util.function.Function;
import java.util.stream.Collectors;

public class WrapperTransformationList<E, F> extends TransformationList<E, F> {

    private final Function<F, E> wrapper;

    public WrapperTransformationList(ObservableList<F> list, Function<F, E> wrapper) {
        super(list);
        this.wrapper = wrapper;
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends F> change) {
        this.beginChange();

        while(change.next()) {
            if (change.wasPermutated()) {
                int[] permutation = new int[change.getTo() - change.getFrom()];
                for(int i = change.getFrom(); i < change.getTo(); i++) {
                    permutation[i - change.getFrom()] = change.getPermutation(i);
                }
                this.nextPermutation(change.getFrom(), change.getTo(), permutation);
            } else if (change.wasUpdated()) {
                this.nextUpdate(change.getFrom());
            } else {
                if(change.wasRemoved())
                    this.nextRemove(change.getFrom(), change.getRemoved().stream()
                            .map(wrapper)
                            .collect(Collectors.toList()));
                if(change.wasAdded())
                    this.nextAdd(change.getFrom(), change.getTo());
            }
        }

        this.endChange();
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
