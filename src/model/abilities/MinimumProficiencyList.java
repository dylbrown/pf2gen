package model.abilities;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import model.attributes.Attribute;
import model.enums.Proficiency;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MinimumProficiencyList extends TransformationList<String, Attribute> implements Iterable<String>, Collection<String>, List<String>, Observable, ObservableList<String> {
    private int[] sourceIndices;
    private boolean[] isInList;
    private int size;
    /**
     * Creates a new Transformation list wrapped around the source list.
     *
     * @param proficiencies the proficiencies to track
     * @param minimum the minimum proficiency to filter by
     */
    public MinimumProficiencyList(Map<Attribute, ReadOnlyObjectWrapper<Proficiency>> proficiencies, Proficiency minimum) {
        super(FXCollections.observableList(Arrays.asList(Attribute.getSkills())));
        size = getSource().size();
        sourceIndices = new int[size];
        isInList = new boolean[size];
        for(int i=0; i<size; i++){
            isInList[i] = true;
            sourceIndices[i] = i;
        }
        for (Map.Entry<Attribute, ReadOnlyObjectWrapper<Proficiency>> entry : proficiencies.entrySet()) {
            int sourceIndex = getSource().indexOf(entry.getKey());
            beginChange();
            if(entry.getValue().get().getMod() < minimum.getMod())
                removeFromList(sourceIndex);
            endChange();
            entry.getValue().addListener((change)->{
                beginChange();
                if(isInList[sourceIndex] && entry.getValue().get().getMod() < minimum.getMod())
                    removeFromList(sourceIndex);
                else if(!isInList[sourceIndex] && entry.getValue().get().getMod() >= minimum.getMod())
                    addToList(sourceIndex);
                endChange();
            });
        }
    }

    private void addToList(int sourceIndex) {
        int index = findSpace(sourceIndex);
        nextAdd(index, index+1);
        for(int i=size; i > index; i--)
            sourceIndices[i] = sourceIndices[i-1];
        sourceIndices[index] = sourceIndex;
        size++;
        isInList[sourceIndex] = true;
    }

    private void removeFromList(int sourceIndex) {
        int index = getViewIndex(sourceIndex);
        nextRemove(index, get(index));
        for(int i=index+1; i < size; i++)
            sourceIndices[i-1] = sourceIndices[i];
        size--;
        isInList[sourceIndex] = false;
    }

    private int findSpace(int sourceIndex) {
        int min = 0; int max = size;
        while(min < max){
            int mid = (min + max) / 2;
            if(sourceIndices[mid] >= sourceIndex)
                max = mid;
            else
                min = mid + 1;
        }
        return min;
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends Attribute> c) {
        //Wraps a Static List, so this cannot happen
    }

    @Override
    public int getSourceIndex(int index) {
        if(index >= size)
            throw new ArrayIndexOutOfBoundsException();
        return sourceIndices[index];
    }

    public int getViewIndex(int sourceIndex) {
        return Arrays.binarySearch(sourceIndices, 0, size, sourceIndex);
    }

    @Override
    public String get(int index) {
        return getSource().get(getSourceIndex(index)).toString();
    }

    @Override
    public int size() {
        return size;
    }
}
