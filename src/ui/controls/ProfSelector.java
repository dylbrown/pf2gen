package ui.controls;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.enums.Attribute;
import model.enums.Proficiency;

import static ui.Main.character;

public class ProfSelector extends HBox {
    private final Attribute skill;
    private final CheckBox[] checkBoxes = {new CheckBox(), new CheckBox(), new CheckBox(), new CheckBox()};
    private final Proficiency[] profs = {Proficiency.Trained, Proficiency.Expert, Proficiency.Master, Proficiency.Legendary};
    private boolean infiniteStopper = false;

    public ProfSelector(Attribute skill) {
        this.skill = skill;
        this.getChildren().addAll(new VBox(new Label("T"), checkBoxes[0]),new VBox(new Label("E"), checkBoxes[1]),new VBox(new Label("M"), checkBoxes[2]),new VBox(new Label("L"), checkBoxes[3]));
        for (Node child : this.getChildren()) {
            if(child instanceof VBox)
                ((VBox) child).setAlignment(Pos.CENTER);
        }


        character.attributes().getProficiency(skill).addListener((event)->updateSelector());
        character.attributes().addObserver((observable, event)->updateSelector());
        for (int i=0; i<checkBoxes.length;i++) {
            int finalI = i;
            checkBoxes[i].setOnAction((event)->handleClick(finalI));
        }
        updateSelector();
    }

    private void handleClick(int i) {
        if(checkBoxes[i].isSelected()){
            if(!character.attributes().advanceSkill(skill))
                updateSelector();
        }else{
            if(!character.attributes().regressSkill(skill))
                updateSelector();
        }
    }

    private void updateSelector() {
        int value = character.attributes().getProficiency(skill).getValue().getMod();
        boolean ticked = true;
        for(int i=0; i<checkBoxes.length;i++) {
            checkBoxes[i].setDisable(true);
            if(ticked && value < profs[i].getMod()) {
                ticked = false;
                if(character.attributes().canAdvanceSkill(skill))
                    checkBoxes[i].setDisable(false);
                if(character.attributes().canRegressSkill(skill))
                    checkBoxes[i-1].setDisable(false);
            }
            checkBoxes[i].setSelected(ticked);
        }

    }
}