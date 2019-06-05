package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abilities.abilitySlots.Choice;

public class DecisionManager {
    private final ObservableList<Choice> decisions = FXCollections.observableArrayList();

    public void remove(Choice choice) {
        decisions.remove(choice);
    }

    public void add(Choice choice) {
        decisions.add(choice);
    }

    public ObservableList<Choice> getDecisions() {
        return FXCollections.unmodifiableObservableList(decisions);
    }
}
