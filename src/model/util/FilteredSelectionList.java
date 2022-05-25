package model.util;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import model.ability_slots.Choice;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FilteredSelectionList<T> extends TransformationList<T, T> {
    private final List<Integer> mapping;
    private final ObservableList<T> selections;
    public FilteredSelectionList(ObservableList<T> options, Choice<T> choice) {
        super(options);
        this.selections = choice.getSelections();
        selections.addListener(this::checkSelections);
        mapping = IntStream.range(0, options.size())
                .filter(i -> !selections.contains(getSource().get(i)))
                .boxed().collect(Collectors.toList());
    }

    private void checkSelections(ListChangeListener.Change<? extends T> change) {
        beginChange();
        while(change.next()) {
            if (change.wasUpdated()) {
                throw new UnsupportedOperationException();
            } else if(!change.wasPermutated()) {
                int current = 0;
                for(int i = 0; i < getSource().size(); i++) {
                    int nextSourceIndex = (mapping.size() > current) ? mapping.get(current) : -1;
                    while(current < mapping.size() && nextSourceIndex < i) {
                        current++;
                        nextSourceIndex = (current < mapping.size()) ? mapping.get(current) : -1;
                    }
                    T t = getSource().get(i);
                    if(change.wasAdded() && nextSourceIndex == i && change.getAddedSubList().contains(t)) {
                        mapping.remove(current);
                        nextRemove(current, Collections.singletonList(t));
                    }else if(change.wasRemoved() && change.getRemoved().contains(t)) {
                        mapping.add(current, i);
                        nextAdd(current, current+1);
                        current++;
                    }
                }
            }
        }
        endChange();
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends T> change) {
        beginChange();
        while(change.next()) {
            int from = translateChangePoint(change.getFrom());
            int to = translateChangePoint(change.getTo());
            if (change.wasPermutated()) {
                if(from >= to) continue;
                int[] permutation = new int[to - from];
                for(int i = from; i < to; i++) {
                    permutation[i - from] = change.getPermutation(mapping.get(i));
                }
                nextPermutation(from, to, permutation);
            } else if (change.wasUpdated()) {
                int current = from;
                for(int i = change.getFrom(); i < change.getTo(); i++) {
                    Integer nextSourceIndex = mapping.get(current);
                    while(current < mapping.size() && nextSourceIndex < i) {
                        current++;
                        nextSourceIndex = (current < mapping.size()) ? mapping.get(current) : -1;
                    }
                    T t = getSource().get(i);
                    if(nextSourceIndex == i) {
                        if(selections.contains(t)) {
                            mapping.remove(current);
                            nextRemove(current, Collections.singletonList(t));
                        }else{
                            nextUpdate(current);
                            current++;
                        }
                    }else{
                        if(!selections.contains(t)) {
                            mapping.add(current, i);
                            nextAdd(current, current+1);
                            current++;
                        }
                    }
                }
            } else {
                if(change.wasRemoved()) {
                    List<T> removed = change.getRemoved().stream().filter(
                            t -> !(selections.contains(t))).collect(Collectors.toUnmodifiableList());
                    mapping.subList(from, from + removed.size()).clear();
                    nextRemove(from, removed);
                }
                if(change.wasAdded()) {
                    List<Integer> added = IntStream.range(change.getFrom(), change.getTo())
                            .filter(i -> !selections.contains(getSource().get(i)))
                            .boxed().collect(Collectors.toUnmodifiableList());
                    mapping.addAll(from, added);
                    nextAdd(from, from + added.size());
                }
            }
        }
        endChange();
    }

    private int translateChangePoint(int from) {
        int result = Collections.binarySearch(mapping, from);
        return (result >= 0) ? result : -(result + 1);
    }

    @Override
    public int getSourceIndex(int i) {
        return mapping.get(i);
    }

    @Override
    public int getViewIndex(int i) {
        int result = Collections.binarySearch(mapping, i);
        return (result >= 0) ? result : -1;
    }

    @Override
    public T get(int index) {
        return getSource().get(mapping.get(index));
    }

    @Override
    public int size() {
        return mapping.size();
    }
}
