package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.abc.Ancestry;
import model.ability_scores.AbilityMod;
import model.xml_parsers.AncestriesLoader;
import ui.Main;

@SuppressWarnings("WeakerAccess")
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
        try{
            ancestryList.getItems().addAll(AncestriesLoader.instance().parse());
        }catch (Exception e){
            e.printStackTrace();
        }
        ancestryList.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                Ancestry selectedItem = ancestryList.getSelectionModel().getSelectedItem();
                Main.character.setAncestry(selectedItem);
            }
        });
        Main.character.getAncestryProperty().addListener((o, oldVal, newVal)-> ancestryDisplay.setText(newVal.getName()));
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
