package ui.controllers;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import model.CharacterManager;
import model.attributes.Attribute;
import model.enums.Proficiency;
import model.player.PC;
import ui.controls.ProfSelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static model.attributes.BaseAttribute.*;

public class SkillTabController {
    private final List<Label> labels = new ArrayList<>(Arrays.asList(new Label[18]));
    private final List<ProfSelector> profSelectors = new ArrayList<>(Arrays.asList(new ProfSelector[18]));
    private final List<Label> totals = new ArrayList<>(Arrays.asList(new Label[18]));
    private final List<Label> abilities = new ArrayList<>(Arrays.asList(new Label[18]));
    private final List<Label> proficiencies = new ArrayList<>(Arrays.asList(new Label[18]));
    private final Attribute[] skills = {Acrobatics, Arcana, Athletics, Crafting, Deception, Diplomacy, Intimidation, Medicine, Nature, Occultism, Performance, Religion, Society, Stealth, Survival, Thievery};
    int halfSize = skills.length / 2;
    private final Label remainingIncreases = new Label("T:0, E:0, M:0, L:0");
    @FXML
    private AnchorPane root;
    private PC character;

    @FXML
    private void initialize() {
        character = CharacterManager.getActive();
        BorderPane border = new BorderPane();
        GridPane grid = new GridPane();
        root.getChildren().add(border);
        border.setCenter(grid);
        border.setBottom(remainingIncreases);


        character.attributes().getSkillIncreases().addListener((MapChangeListener<Integer,Integer>) change -> updateLabel());
        character.levelProperty().addListener(change -> updateLabel());
        character.pClassProperty().addListener(change -> updateLabel());
        character.scores().addAbilityListener((o)->updateLabel());
        character.attributes().addListener((o)->updateLabel());
        updateLabel();

        AnchorPane.setLeftAnchor(border, 15.0);
        AnchorPane.setRightAnchor(border, 15.0);
        AnchorPane.setTopAnchor(border, 0.0);
        grid.setStyle("-fx-hgap: 10;-fx-alignment:center;");
        ColumnConstraints grow = new ColumnConstraints();
        grow.setHgrow(Priority.SOMETIMES);
        ColumnConstraints stay = new ColumnConstraints();
        for(int i=0;i<16;i++) {
            if(i == 0 || i == halfSize || i == skills.length - 1)
                grid.getColumnConstraints().add(grow);
            else
                grid.getColumnConstraints().add(stay);
        }

        for(int i = 0; i < halfSize; i++) {
            Attribute leftSkill = skills[i];
            Attribute rightSkill = skills[i+halfSize];

            labels.set(i, new Label(leftSkill.toString()));
            labels.set(i+halfSize, new Label(rightSkill.toString()));
            profSelectors.set(i, new ProfSelector(leftSkill, character));
            profSelectors.set(i+halfSize, new ProfSelector(rightSkill, character));
            totals.set(i, new Label());
            totals.get(i).setStyle("-fx-border-color:black;");
            totals.set(i+halfSize, new Label());
            abilities.set(i, new Label());
            abilities.set(i+halfSize, new Label());
            proficiencies.set(i, new Label());
            proficiencies.set(i+halfSize, new Label());
            grid.addRow(i,new Label(), labels.get(i), new VBox(new Label("Total"),totals.get(i)),
                    new Label("="),
                    new VBox(new Label(leftSkill.getKeyAbility().toString()),abilities.get(i)),
                    new Label("+"),
                    new VBox(new Label("Prof"),proficiencies.get(i)),
                    profSelectors.get(i),
                    new Label(),//Right Side
                    labels.get(i+halfSize), new VBox(new Label("Total"),totals.get(i+halfSize)),
                    new Label("="),
                    new VBox(new Label(rightSkill.getKeyAbility().toString()),abilities.get(i+halfSize)),
                    new Label("+"),
                    new VBox(new Label("Prof"),proficiencies.get(i+halfSize)),
                    profSelectors.get(i+halfSize));
        }
        for(int i=0; i<skills.length; i++) {
            totals.get(i).setStyle("-fx-border-color:black;-fx-padding: 5;");
            abilities.get(i).setStyle("-fx-border-color:black;-fx-padding: 5;");
            proficiencies.get(i).setStyle("-fx-border-color:black;-fx-padding: 5;");
        }
        character.scores().addAbilityListener((observable)-> updateTab());
        character.attributes().addListener((observable)-> updateTab());
        updateTab();
    }

    private void updateTab() {
        for(int i=0; i<skills.length; i++) {
            int abilityMod = character.scores().getMod(skills[i].getKeyAbility());
            int proficiencyMod = character.attributes().getProficiency(skills[i]).getValue().getMod(character.getLevel());
            totals.get(i).setText(String.valueOf(abilityMod+proficiencyMod));
            abilities.get(i).setText(String.valueOf(abilityMod));
            proficiencies.get(i).setText(String.valueOf(proficiencyMod));
        }
    }

    private void updateLabel() {
        ObservableMap<Integer, Integer> skillIncreases = character.attributes().getSkillIncreases();
        int[] remaining = new int[4];
        int[] increases = new int[4];
        increases[0] = skillIncreases.getOrDefault(1,0);
        remaining[0] = character.attributes().getSkillIncreasesRemaining(1);
        for(int level=2; level<=character.getLevel(); level++) {
            if(level < Proficiency.getMinLevel(Proficiency.Master)){
                increases[1] += skillIncreases.getOrDefault(level, 0);
                remaining[1] += character.attributes().getSkillIncreasesRemaining(level);
            }else if (level < Proficiency.getMinLevel(Proficiency.Legendary)){
                increases[2] += skillIncreases.getOrDefault(level, 0);
                remaining[2] += character.attributes().getSkillIncreasesRemaining(level);
            }else{
                increases[3] += skillIncreases.getOrDefault(level, 0);
                remaining[3] += character.attributes().getSkillIncreasesRemaining(level);
            }
        }
        remainingIncreases.setText("T:"+remaining[0]+"/"+increases[0]
                +", E:"+remaining[1]+"/"+increases[1]
                +", M:"+remaining[2]+"/"+increases[2]
                +", L:"+remaining[3]+"/"+increases[3]);
    }
}
