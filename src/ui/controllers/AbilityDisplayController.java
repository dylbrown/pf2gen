package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import model.CharacterManager;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityScore;
import model.enums.Type;
import model.player.PC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbilityDisplayController {
    @FXML
    private GridPane gridPane;
    private final List<Label> boxes = new ArrayList<>();
    private PC character;

    @FXML
    private void initialize() {
        character = CharacterManager.getActive();
        for(Node node: gridPane.getChildren()) {
            if(!(node instanceof Label))
                continue;
            Label label = (Label) node;
            Integer row = GridPane.getRowIndex(label);
            Integer col = GridPane.getColumnIndex(label);
            if(row != null && (row == 1 || row == 2) && col != null && col != 0)
                boxes.add((Label) node);
        }
        updateTable();
        character.scores().addAbilityListener((observable)->updateTable());
    }
    private final boolean[] added = {false, false, false, false, false, false, false, false};
    private void updateTable() {
        Type[] relevantTypes = {Type.Ancestry, Type.Background, Type.Class, Type.Initial, Type.Five, Type.Ten, Type.Fifteen, Type.Twenty};
        String[] labels = {"Ancestry", "Background", "PClass", "Free", "Level 5", "Level 10", "Level 15", "Level 20"};

        for (int i=0; i<added.length; i++) {
            if(added[i])
                continue;
            if(character.scores().getAbilityMods(relevantTypes[i]).size() > 0){
                added[i] = true;
                List<Label> list = Arrays.asList(new Label(" "), new Label(" "), new Label(" "), new Label(" "), new Label(" "), new Label(" "));
                for (Label label : list) {
                    //label.setStyle("-fx-font-size:18pt;");
                    label.getStyleClass().add("regular-color");
                    label.setMaxWidth(1.7976931348623157E308);
                    label.setMaxHeight(1.7976931348623157E308);
                    label.setAlignment(Pos.CENTER);
                }
                boxes.addAll(list);
                Label label = new Label(labels[i]);
                label.getStyleClass().add("inverted-color");
                label.setMaxWidth(1.7976931348623157E308);
                label.setMaxHeight(1.7976931348623157E308);
                label.setAlignment(Pos.CENTER);
                gridPane.addRow(3+i, label, list.get(0), list.get(1), list.get(2), list.get(3), list.get(4), list.get(5));
            }
        }

        for(Label label: boxes) {
            Integer row = GridPane.getRowIndex(label);
            Integer col = GridPane.getColumnIndex(label);
            switch(row) {
                case 1: // Scores
                    label.setText(String.valueOf(character.scores().getScore(AbilityScore.scores().get(col-1))));
                    break;
                case 2: // Modifiers
                    int abilityMod = character.scores().getMod(AbilityScore.scores().get(col-1));
                    if (abilityMod > 0)
                        label.setText("+" + abilityMod);
                    else
                        label.setText(String.valueOf(abilityMod));
                    break;
                default:
                    label.setText(" ");
                    int checker=0;
                    for (AbilityMod mod : character.scores().getAbilityMods(relevantTypes[row - 3])) {
                        if(mod.getTarget().equals(AbilityScore.scores().get(col-1))){
                            if(mod.isPositive()) {
                                if(checker == 2)
                                    checker = 3;
                                else
                                    checker = 1;
                            }else{
                                if(checker == 1)
                                    checker = 3;
                                else
                                    checker = 2;
                            }
                        }
                        switch (checker){
                            case 1:
                                label.setText("⬆");
                                break;
                            case 2:
                                label.setText("⬇");
                                break;
                            case 3:
                                label.setText("⬍");
                                break;
                        }
                    }
                    break;
            }
        }
    }
}
