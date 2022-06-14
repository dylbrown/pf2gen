package model.player;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import model.ability_slots.Choice;
import model.ability_slots.ChoiceList;

import java.util.Comparator;
import java.util.List;

public class DecisionManager implements PlayerState {
    private final ObservableList<Choice<?>> decisions = FXCollections.observableArrayList(
            choice -> new Observable[]{choice.numSelectionsProperty(), choice.maxSelectionsProperty()});
    private final ObservableList<Choice<?>> unmodifiableDecisions = FXCollections.unmodifiableObservableList(decisions);
    private final FilteredList<Choice<?>> unmade;
    private final SortedList<Choice<?>> unmadeByLevel;

    DecisionManager() {
        unmade = new FilteredList<>(decisions, choice -> choice.getSelections().size() < choice.getMaxSelections());
        unmadeByLevel = new SortedList<>(unmade, Comparator.comparingInt(Choice::getLevel));
    }

    public <T> void remove(Choice<T> choice) {
        decisions.remove(choice);

    }

    public <T> void add(Choice<T> choice) {
        decisions.add(choice);
        if(choice instanceof ChoiceList) {
            List<T> options = ((ChoiceList<T>) choice).getOptions();
            if(options.size() == 1 && choice.getSelections().size() == 0) {
                choice.add(options.get(0));
            }
        }
    }

    public ObservableList<Choice<?>> getDecisions() {
        return unmodifiableDecisions;
    }

    public Choice<?> getNextUnmadeDecision() {
        return unmadeByLevel.get(0);
    }

    public ObservableList<Choice<?>> getUnmadeDecisions() {
        return FXCollections.unmodifiableObservableList(unmade);
    }

    @Override
    public void reset(PC.ResetEvent resetEvent) {

    }
}
