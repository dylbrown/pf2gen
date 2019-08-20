package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abilities.abilitySlots.Choice;

import java.util.*;

public class DecisionManager {
    private final ObservableList<Choice> decisions = FXCollections.observableArrayList();
    private final SortedMap<Integer, SortedMap<String, Choice>> decisionMap = new TreeMap<>();

    public void remove(Choice choice) {
        decisions.remove(choice);
        decisionMap.computeIfAbsent(choice.getLevel(), (key)->
                new TreeMap<>()).remove(choice.toString(), choice);
    }

    public void add(Choice choice) {
        decisions.add(choice);
        decisionMap.computeIfAbsent(choice.getLevel(), (key)->
                new TreeMap<>()).put(choice.toString(), choice);
    }

    public ObservableList<Choice> getDecisions() {
        return FXCollections.unmodifiableObservableList(decisions);
    }

    public Choice getNextUnmadeDecision() {
        for (Map.Entry<Integer, SortedMap<String, Choice>> levelEntry : decisionMap.entrySet()) {
            for(Map.Entry<String, Choice> entry: levelEntry.getValue().entrySet()) {
                if(entry.getValue().getChoice() == null)
                    return entry.getValue();
            }
        }
        return null;
    }

    public List<Choice> getUnmadeDecisions() {
        List<Choice> choices = new ArrayList<>();
        for (Map.Entry<Integer, SortedMap<String, Choice>> levelEntry : decisionMap.entrySet()) {
            for(Map.Entry<String, Choice> entry: levelEntry.getValue().entrySet()) {
                if(entry.getValue().getChoice() == null)
                    choices.add(entry.getValue());
            }
        }
        return choices;
    }
}
