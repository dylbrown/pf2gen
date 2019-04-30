package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.FileLoader;
import model.abc.Ancestry;
import model.abilityScores.AbilityMod;
import ui.Main;

public class AncestryTabController {
    @FXML
    private Label hp;
    @FXML
    private Label size;
    @FXML
    private Label speed;
    @FXML
    private Label bonuses;
    @FXML
    private Label penalties;
    @FXML
    private ListView<Ancestry> ancestryList;
    @FXML
    private Button setAncestry;
    @FXML
    private Label ancestryDisplay;
    @FXML
    private Label ancestryDesc;
    @FXML
    private void initialize() {
        ancestryList.getItems().addAll(FileLoader.getAncestries());

        setAncestry.setOnAction((event) -> {
            Ancestry selectedItem = ancestryList.getSelectionModel().getSelectedItem();
            ancestryDisplay.setText(selectedItem.toString());
            Main.character.setAncestry(selectedItem);
        });
        ancestryList.getSelectionModel().selectedItemProperty().addListener((event)->{
            Ancestry item = ancestryList.getSelectionModel().getSelectedItem();
            ancestryDesc.setText(item.getDesc());
            hp.setText(String.valueOf(item.getHP()));
            size.setText(item.getSize().toString());
            speed.setText(item.getSpeed() + " feet");
            StringBuilder bonusBuilder = new StringBuilder();
            StringBuilder penaltyBuilder = new StringBuilder();
            for (AbilityMod abilityMod : item.getAbilityMods()) {
                if(abilityMod.isPositive()){
                    bonusBuilder.append(abilityMod.getTarget()).append("\n");
                }else{
                    penaltyBuilder.append(abilityMod.getTarget()).append("\n");
                }
            }
            if(bonusBuilder.length() > 0) bonusBuilder.deleteCharAt(bonusBuilder.length()-1);
            else bonusBuilder.append("-");
            if(penaltyBuilder.length() > 0) penaltyBuilder.deleteCharAt(penaltyBuilder.length()-1);
            else penaltyBuilder.append("-");
            bonuses.setText(bonusBuilder.toString());
            penalties.setText(penaltyBuilder.toString());
        });
    }
}
