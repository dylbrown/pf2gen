package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import model.abilities.abilitySlots.Choice;

import java.util.*;

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

    public void add(Choice choice) {
        decisions.add(choice);
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
