package ui.controllers;

import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.Activity;
import model.enums.Action;
import model.equipment.Equipment;
import model.equipment.ItemTrait;
import model.equipment.RangedWeapon;
import model.equipment.Weapon;
import ui.TemplateFiller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ui.Main.character;

public class Controller {
    @FXML
    private ListView<Class> classList;
    @FXML
    private TextField characterName;
    @FXML
    private Tab displayTab;
    @FXML
    private WebView display;
    @FXML
    private Label level;
    @FXML
    private Button levelUp;
    private String htmlContent;


    @FXML
    private void initialize(){

        export.setOnAction((event -> {
            FileChooser fileChooser = new FileChooser();
            if(!Objects.equals(characterName.getText(), ""))
                fileChooser.setInitialFileName(characterName.getText().replaceAll(" ",""));
            fileChooser.setInitialDirectory(new File("./"));
            //Set extension filter for text files
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("HTML files", "*.html")
            );

            //Show save file dialog
            File file = fileChooser.showSaveDialog(export.getScene().getWindow());

            if (file != null) {
                try {
                    PrintWriter out = new PrintWriter(file);
                    out.println(htmlContent);
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }));




        classList.getItems().addAll(FileLoader.getClasses());

        characterName.textProperty().addListener((observable, oldValue, newValue) -> character.setName(newValue));
        displayTab.setOnSelectionChanged((event) -> {
            if(displayTab.isSelected()) {
                htmlContent = TemplateFiller.getStatBlock();
                display.getEngine().loadContent(htmlContent);
            }
        });
        level.setText("0");
        character.getLevelProperty().addListener((event)-> level.setText(character.getLevelProperty().get().toString()));
        levelUp.setOnAction((event -> character.levelUp()));
    }

    private String getWeaponAttacks() {
        ObservableMap<Equipment, Equipment> inventory = character.inventory().getItems();
        List<String> items = new ArrayList<>();
        for (Map.Entry<Equipment, Equipment> entry : inventory.entrySet()) {
            if(entry.getKey() instanceof Weapon){
                Weapon weapon = (Weapon) entry.getKey();
                StringBuilder weaponBuilder = new StringBuilder();
                if(weapon instanceof RangedWeapon)
                    weaponBuilder.append("<b>① Ranged</b> ");
                else
                    weaponBuilder.append("<b>① Melee</b> ");
                int attackMod = character.getAttackMod(weapon);
                if(attackMod >= 0)
                    weaponBuilder.append(weapon.getName()).append(" +").append(attackMod);
                else
                    weaponBuilder.append(weapon.getName()).append(" ").append(attackMod);
                if(weapon.getTraits().size() > 0) {
                    List<String> traits = new ArrayList<>();
                    for (ItemTrait trait : weapon.getTraits()) {
                        traits.add(trait.getName());
                    }
                    weaponBuilder.append(" (").append(String.join(", ", traits)).append(")");
                }
                weaponBuilder.append(", <b>Damage</b> ").append(weapon.getDamage().toString());
                int damageMod = character.getDamageMod(weapon);
                if(damageMod > 0)
                    weaponBuilder.append("+").append(damageMod);
                else if(damageMod < 0)
                    weaponBuilder.append(damageMod);
                weaponBuilder.append(" ").append(weapon.getDamageType().toString());
                items.add(weaponBuilder.toString());
            }
        }
        return String.join("<br>", items);
    }
    private String getAbilities() {
        List<String> items = new ArrayList<>();
        for (Ability ability : character.getAbilities()) {
            if(ability instanceof Activity) {
                items.add(getAbility(ability));
            }else if(ability instanceof AbilitySet)
                items.addAll(getAbilities(((AbilitySet) ability).getAbilities()));

        }
        return String.join("<br>", items);
    }

    private List<String> getAbilities(List<Ability> abilities) {
        List<String> items = new ArrayList<>();
        for (Ability ability: abilities) {
            if(ability instanceof Activity)
                items.add(getAbility(ability));
            else if(ability instanceof AbilitySet)
                items.addAll(getAbilities(((AbilitySet) ability).getAbilities()));

        }
        return items;
    }

    private String getAbility(Ability ability) {
        StringBuilder abilityBuilder = new StringBuilder();
        abilityBuilder.append("<b>");
        switch (((Activity) ability).getCost()) {
            case Free:
                abilityBuilder.append("Ⓕ ");
                break;
            case Reaction:
                abilityBuilder.append("Ⓡ ");
                break;
            case One:
                abilityBuilder.append("① ");
                break;
            case Two:
                abilityBuilder.append("② ");
                break;
            case Three:
                abilityBuilder.append("③ ");
                break;
        }
        abilityBuilder.append(ability.toString()).append("</b> ").append(ability.getDesc());
        if(((Activity) ability).getCost() == Action.Reaction)
            abilityBuilder.append(" <b>Trigger</b> ").append(((Activity) ability).getTrigger());
        return abilityBuilder.toString();
    }

    private String generateItemList() {
        ObservableMap<Equipment, Equipment> inventory = character.inventory().getItems();
        List<String> items = new ArrayList<>();
        for (Map.Entry<Equipment, Equipment> entry : inventory.entrySet()) {
            if(entry.getValue().getValue() > 1)
                items.add(entry.getValue()+" "+entry.getKey().getName());
            else
                items.add(entry.getKey().getName());
        }
        return String.join(", ", items);
    }

    private String prettyPrintLanguages() {
        return character.getLanguages().stream().map(Enum::toString).collect(Collectors.joining(", "));
    }

    private String addSign(int mod) {
        return ((mod >= 0) ? "+" : "")+ mod;
    }
}
