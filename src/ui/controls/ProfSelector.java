package ui.controls;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.attributes.Attribute;
import model.enums.Proficiency;
import model.player.PC;

public class ProfSelector extends HBox {
    private final Attribute skill;
    private final PC character;
    private String data;
    private final CheckBox[] checkBoxes = {new CheckBox(), new CheckBox(), new CheckBox(), new CheckBox()};
    private final Proficiency[] profs = {Proficiency.Trained, Proficiency.Expert, Proficiency.Master, Proficiency.Legendary};

    public ProfSelector(Attribute skill, String data, PC character) {
        this.character = character;
        this.skill = skill;
        this.data = data;
        this.getChildren().addAll(new VBox(new Label("T"), checkBoxes[0]),new VBox(new Label("E"), checkBoxes[1]),new VBox(new Label("M"), checkBoxes[2]),new VBox(new Label("L"), checkBoxes[3]));
        for (Node child : this.getChildren()) {
            if(child instanceof VBox)
                ((VBox) child).setAlignment(Pos.CENTER);
        }


        character.attributes().getProficiency(skill, data).addListener((event)->updateSelector());
        character.attributes().addListener((observable)->updateSelector());
        for (int i=0; i<checkBoxes.length;i++) {
            int finalI = i;
            checkBoxes[i].setOnAction((event)->handleClick(finalI));
        }
        updateSelector();


        if(skill == Attribute.Lore && (data == null || data.equals(""))) {
            for (CheckBox checkBox : checkBoxes) {
                checkBox.setDisable(true);
            }
        }
    }

    private void handleClick(int i) {
        if(checkBoxes[i].isSelected()){
            if(!character.attributes().advanceSkill(skill, data))
                updateSelector();
        }else{
            if(!character.attributes().regressSkill(skill, data))
                updateSelector();
        }
    }

    private void updateSelector() {
        if(skill == Attribute.Lore && (data == null || data.equals(""))) {
            return;
        }
        int value = character.attributes().getProficiency(skill, data).getValue().getMod();
        boolean ticked = true;
        for(int i=0; i<checkBoxes.length;i++) {
            checkBoxes[i].setDisable(true);
            if(ticked && value < profs[i].getMod()) {
                ticked = false;
                if(character.attributes().canAdvanceSkill(skill, data))
                    checkBoxes[i].setDisable(false);
                if(character.attributes().canRegressSkill(skill, data))
                    checkBoxes[i-1].setDisable(false);
            }
            checkBoxes[i].setSelected(ticked);
        }

    }

    public void setData(String s) {
        this.data = s;
        updateSelector();
    }
}
