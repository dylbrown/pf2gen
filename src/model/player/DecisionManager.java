package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import model.ability_slots.Choice;
import model.ability_slots.ChoiceList;

import java.util.*;

@SuppressWarnings("rawtypes")
public class DecisionManager {
    private final ObservableList<Choice> decisions = FXCollections.observableArrayList();
    private final ObservableList<Choice> unmodifiableDecisions = FXCollections.unmodifiableObservableList(decisions);
    private final FilteredList<Choice> unmade;
    private final SortedList<Choice> unmadeByLevel;

    DecisionManager() {
        unmade = new FilteredList<>(decisions, choice -> choice.getSelections().size() < choice.getNumSelections());
        unmadeByLevel = new SortedList<>(unmade, Comparator.comparingInt(Choice::getLevel));
    }

    public void remove(Choice choice) {
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

    public ObservableList<Choice> getDecisions() {
        return unmodifiableDecisions;
    }

    public Choice getNextUnmadeDecision() {
        return unmadeByLevel.get(0);
    }

    public ObservableList<Choice> getUnmadeDecisions() {
        return FXCollections.unmodifiableObservableList(unmade);
    }
}
