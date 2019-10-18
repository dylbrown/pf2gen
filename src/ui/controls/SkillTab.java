package ui.controls;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import model.enums.Attribute;
import model.enums.Proficiency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static model.enums.Attribute.*;
import static ui.Main.character;

public class SkillTab extends AnchorPane {
    private final List<Label> totals = new ArrayList<>(Arrays.asList(new Label[18]));
    private final List<Label> abilities = new ArrayList<>(Arrays.asList(new Label[18]));
    private final List<Label> proficiencies = new ArrayList<>(Arrays.asList(new Label[18]));
    private final Attribute[] skills = {Acrobatics, Arcana, Athletics, Crafting, Deception, Diplomacy, Intimidation, Lore, Lore, Medicine, Nature, Occultism, Performance, Religion, Society, Stealth, Survival, Thievery};
    private Label remainingIncreases = new Label("T:0, E:0, M:0, L:0");

    public SkillTab() {
        BorderPane border = new BorderPane();
        GridPane grid = new GridPane();
        this.getChildren().add(border);
        border.setCenter(grid);
        border.setBottom(remainingIncreases);

        character.attributes().getSkillIncreases().addListener((MapChangeListener<Integer,Integer>) change -> updateLabel());
        character.getLevelProperty().addListener(change -> updateLabel());
        character.getPClassProperty().addListener(change -> updateLabel());
        character.scores().addAbilityObserver((o, arg)->updateLabel());
        character.attributes().addObserver((o, arg)->updateLabel());
        updateLabel();

        AnchorPane.setLeftAnchor(border, 15.0);
        AnchorPane.setRightAnchor(border, 15.0);
        AnchorPane.setTopAnchor(border, 0.0);
        grid.setStyle("-fx-hgap: 10;-fx-alignment:center;");
        ColumnConstraints grow = new ColumnConstraints();
        grow.setHgrow(Priority.SOMETIMES);
        ColumnConstraints stay = new ColumnConstraints();
        for(int i=0;i<16;i++) {
            if(i == 0 || i==8 || i==15)
                grid.getColumnConstraints().add(grow);
            else
                grid.getColumnConstraints().add(stay);
        }

        for(int i=0; i<9; i++) {
            Attribute leftSkill = skills[i];
            Attribute rightSkill = skills[i+9];
            totals.set(i, new Label());
            totals.get(i).setStyle("-fx-border-color:black;");
            totals.set(i+9, new Label());
            abilities.set(i, new Label());
            abilities.set(i+9, new Label());
            proficiencies.set(i, new Label());
            proficiencies.set(i+9, new Label());
            grid.addRow(i,new Label(), new Label(leftSkill.toString()), new VBox(new Label("Total"),totals.get(i)),
                    new Label("="),
                    new VBox(new Label(leftSkill.getKeyAbility().toString()),abilities.get(i)),
                    new Label("+"),
                    new VBox(new Label("Prof"),proficiencies.get(i)),
                    new ProfSelector(leftSkill),
                    new Label(),//Right Side
                    new Label(rightSkill.toString()), new VBox(new Label("Total"),totals.get(i+9)),
                    new Label("="),
                    new VBox(new Label(rightSkill.getKeyAbility().toString()),abilities.get(i+9)),
                    new Label("+"),
                    new VBox(new Label("Prof"),proficiencies.get(i+9)),
                    new ProfSelector(rightSkill),new Label());
        }
        for(int i=0; i<18; i++) {
            totals.get(i).setStyle("-fx-border-color:black;-fx-padding: 5;");
            abilities.get(i).setStyle("-fx-border-color:black;-fx-padding: 5;");
            proficiencies.get(i).setStyle("-fx-border-color:black;-fx-padding: 5;");
        }
        character.scores().addAbilityObserver((observable, arg)-> updateTab());
        character.attributes().addObserver((observable, arg)-> updateTab());
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
