package ui.controls;

import javafx.scene.control.Label;
import javafx.scene.layout.*;
import model.enums.Attribute;

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

    public SkillTab() {
        GridPane grid = new GridPane();
        this.getChildren().add(grid);
        AnchorPane.setLeftAnchor(grid, 15.0);
        AnchorPane.setRightAnchor(grid, 15.0);
        AnchorPane.setTopAnchor(grid, 0.0);
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
        character.addAbilityObserver((observable, arg)-> updateTab());
        character.addProficiencyObserver((observable, arg)-> updateTab());
        updateTab();
    }

    private void updateTab() {
        for(int i=0; i<skills.length; i++) {
            int abilityMod = character.getAbilityMod(skills[i].getKeyAbility());
            int proficiencyMod = character.getProficiency(skills[i]).getValue().getMod();
            totals.get(i).setText(String.valueOf(abilityMod+proficiencyMod));
            abilities.get(i).setText(String.valueOf(abilityMod));
            proficiencies.get(i).setText(String.valueOf(proficiencyMod));
        }
    }
}
