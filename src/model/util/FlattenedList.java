package model.util;

import com.sun.javafx.collections.SourceAdapterChange;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.WeakListChangeListener;

import java.util.*;

public class FlattenedList<T> extends ObservableListBase<T> implements ObservableList<T> {

    private final ObservableList<ObservableList<T>> deepList;
    private final Map<StrictKey, Integer> listOffset = new HashMap<>();
    private int totalSize = 0;
    public FlattenedList(ObservableList<ObservableList<T>> deepList) {
        this.deepList = deepList;

        deepList.addListener(this::updateChanged);
        for (ObservableList<T> child : deepList) {
            listOffset.put(new StrictKey(child), totalSize);
            totalSize += child.size();
            child.addListener(this::childUpdateChanged);
        }
    }

    private int getOffset(ObservableList<? extends T> list) {
        return listOffset.get(new StrictKey(list));
    }

    private int getOffset(int index) {
        if(index >= deepList.size())
            return totalSize;
        if(index < 0)
            return 0;
        return getOffset(deepList.get(index));
    }

    /**
     * This method passes along changes to the parent list (deepList)
     * @param change The change to deep list
     */
    private void updateChanged(ListChangeListener.Change<? extends ObservableList<T>> change) {
        this.beginChange();

        while(change.next()) {
            if (change.wasPermutated()) {
                // These lists contain the permutation of the child lists in terms of the deep list
                SortedMap<Integer, Integer> bigPermutation = new TreeMap<>();
                SortedMap<Integer, Integer> bigPermutationReversed = new TreeMap<>();
                for(int i = change.getFrom(); i < change.getTo(); i++) {
                    bigPermutation.put(i, change.getPermutation(i));
                    bigPermutationReversed.put(change.getPermutation(i), i);
                }

                // These are in terms of the flattened indices
                int fromOffset = getOffset(bigPermutation.get(bigPermutation.firstKey()));
                int toOffset = getOffset(change.getTo());
                int[] permutation = new int[toOffset - fromOffset];
                int newOffset = fromOffset;

                // Update in the new order of the child lists from front to back
                for (Map.Entry<Integer, Integer> entry : bigPermutationReversed.entrySet()) {
                    ObservableList<T> list = deepList.get(entry.getKey());
                    int oldOffset = getOffset(list);

                    // Since the permutation is in terms of the flattened list, we actually have (size) permutations
                    for(int j = 0; j < list.size(); j++) {
                        permutation[j + (oldOffset - fromOffset)] = j + newOffset;
                    }
                    listOffset.put(new StrictKey(list), newOffset);
                    newOffset += list.size();
                }

                this.nextPermutation(fromOffset, toOffset, permutation);
            } else if (!change.wasUpdated()) { // Update handled by child update checks
                int sizeChanged = 0;
                if(change.wasRemoved()) {
                    int removedIndex = getOffset(change.getRemoved().get(0));
                    List<T> removed = new ArrayList<>();
                    for (ObservableList<T> child : change.getRemoved()) {
                        child.removeListener(this::childUpdateChanged);
                        sizeChanged -= listOffset.remove(new StrictKey(child));
                        removed.addAll(child);
                    }
                    this.nextRemove(removedIndex, removed);
                }
                if(change.wasAdded()) {
                    int sizeAdded = 0;
                    int baseOffset = getOffset(change.getFrom() - 1);
                    for (ObservableList<T> child : change.getAddedSubList()) {
                        child.addListener(this::childUpdateChanged);
                        listOffset.put(new StrictKey(child), baseOffset + sizeAdded);
                        sizeAdded += child.size();
                    }
                    sizeChanged += sizeAdded;
                    int start = getOffset(change.getFrom());
                    this.nextAdd(start, start + sizeAdded);
                }
                if(sizeChanged != 0) {
                    // Update the offset of everything after the point
                    for(int i = change.getTo(); i < deepList.size(); i++) {
                        listOffset.merge(new StrictKey(deepList.get(i)), sizeChanged, Integer::sum);
                    }
                    totalSize += sizeChanged;
                }
            }
        }

        this.endChange();
    }

    /**
     * This method passes along changes to one of the child lists of deepList
     * @param change The change to a child list
     */
    private void childUpdateChanged(ListChangeListener.Change<? extends T> change) {
        ObservableList<? extends T> list = change.getList();
        this.beginChange();

        while(change.next()) {
            int offset = getOffset(list);
            if (change.wasPermutated()) {
                int[] permutation = new int[change.getTo() - change.getFrom()];
                for(int i = change.getFrom(); i < change.getTo(); i++) {
                    permutation[i - change.getFrom()] = change.getPermutation(i) + offset;
                }
                this.nextPermutation(change.getFrom() + offset, change.getTo() + offset, permutation);
            } else if (change.wasUpdated()) {
                this.nextUpdate(change.getFrom() + offset);
            } else {
                if(change.wasRemoved())
                    this.nextRemove(change.getFrom() + offset, change.getRemoved());
                if(change.wasAdded())
                    this.nextAdd(change.getFrom() + offset, change.getTo() + offset);

                //Similar to the parent version, update the offsets of everything after
                int offsetChange = change.getAddedSize() - change.getRemovedSize();
                if(offsetChange != 0) {
                    ListIterator<ObservableList<T>> it = deepList.listIterator(deepList.size());
                    while (it.hasPrevious()) {
                        ObservableList<T> curr = it.previous();
                        if (curr == list)
                            break;
                        listOffset.merge(new StrictKey(curr), offsetChange, Integer::sum);
                    }
                    totalSize += offsetChange;
                }
            }
        }

        this.endChange();
    }

    /**
     * Gets the contents of the correct child list via flattened indices
     * @param index the index to access
     * @return returns the item from the child list corresponding to the flattened index
     * @throws ArrayIndexOutOfBoundsException if the index is off either end
     * @performance O(deepList.size)
     */
    @Override
    public T get(int index) {
        if(index < 0 || index >= totalSize)
            throw new ArrayIndexOutOfBoundsException(index);
        for (ObservableList<T> child : deepList) {
            if(child.size() > index)
                return child.get(index);
            index -= child.size();
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public int size() {
        return totalSize;
    }

    // This is simply here so we can get unique hashes per-list
    @SuppressWarnings("unchecked")
    private class StrictKey extends ObservableListBase<T> implements ObservableList<T> {
        private final ObservableList<? extends T> backingList;
        @SuppressWarnings("FieldCanBeLocal")
        private final ListChangeListener<T> listener;

        public StrictKey(ObservableList<? extends T> var1) {
            this.backingList = var1;
            this.listener = (var1x) -> this.fireChange(new SourceAdapterChange<>(this, var1x));
            this.backingList.addListener(new WeakListChangeListener<>(this.listener));
        }

        public T get(int var1) {
            return this.backingList.get(var1);
        }

        public int size() {
            return this.backingList.size();
        }

        public boolean addAll(T... var1) {
            throw new UnsupportedOperationException();
        }

        public boolean setAll(T... var1) {
            throw new UnsupportedOperationException();
        }

        public boolean setAll(Collection<? extends T> var1) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(T... var1) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(T... var1) {
            throw new UnsupportedOperationException();
        }

        public void remove(int var1, int var2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            StrictKey strictKey = (StrictKey) o;

            return this.backingList == strictKey.backingList;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.backingList);
        }
    }
}
