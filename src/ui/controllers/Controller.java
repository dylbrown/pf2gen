package ui.controllers;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import model.abilityScores.AbilityScore;
import model.enums.Attribute;
import model.enums.Proficiency;
import model.equipment.Equipment;
import model.equipment.ItemTrait;
import model.equipment.RangedWeapon;
import model.equipment.Weapon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
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

    private static final String htmlTemplate;
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
                htmlContent = String.format(htmlTemplate, character.getName(),
                        (character.currentClass()==null)?"No Class":character.currentClass().toString(),
                        character.getLevel().get(),
                        addSign(character.getTotalMod(Attribute.Perception)),
                        prettyPrintLanguages(),
                        prettyPrintSkills(),
                        addSign(character.getAbilityMod(AbilityScore.Str)),
                        addSign(character.getAbilityMod(AbilityScore.Dex)),
                        addSign(character.getAbilityMod(AbilityScore.Con)),
                        addSign(character.getAbilityMod(AbilityScore.Int)),
                        addSign(character.getAbilityMod(AbilityScore.Wis)),
                        addSign(character.getAbilityMod(AbilityScore.Cha)),
                        generateItemList(),
                        character.getAC(), character.getTAC(),
                        addSign(character.getTotalMod(Attribute.Fortitude)),
                        addSign(character.getTotalMod(Attribute.Reflex)),
                        addSign(character.getTotalMod(Attribute.Will)),
                        character.getHP(),
                        character.getSpeed(),
                        getWeaponAttacks()
                );
                display.getEngine().loadContent(htmlContent);
            }
        });
        level.setText("0");
        character.getLevel().addListener((event)->{
            level.setText(character.getLevel().get().toString());
        });
        levelUp.setOnAction((event -> character.levelUp()));
    }

    private String getWeaponAttacks() {
        ObservableMap<Equipment, Equipment> inventory = character.getInventory();
        List<String> items = new ArrayList<>();
        for (Map.Entry<Equipment, Equipment> entry : inventory.entrySet()) {
            if(entry.getKey() instanceof Weapon){
                Weapon weapon = (Weapon) entry.getKey();
                StringBuilder weaponBuilder = new StringBuilder();
                if(weapon instanceof RangedWeapon)
                    weaponBuilder.append("<b>Ranged</b> ");
                else
                    weaponBuilder.append("<b>Melee</b> ");
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

    private String generateItemList() {
        ObservableMap<Equipment, Equipment> inventory = character.getInventory();
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

    private String prettyPrintSkills() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Attribute, ObservableValue<Proficiency>> entry : character.getProficiencies().entrySet()) {
            if(Arrays.asList(Attribute.getSkills()).contains(entry.getKey()) && entry.getValue().getValue() != Proficiency.Untrained)
                builder.append(entry.getKey().name()).append(" ").append(addSign(character.getTotalMod(entry.getKey()))).append(", ");
        }
        if(builder.length() > 0)
            builder.deleteCharAt(builder.length()-1);
        if(builder.length() > 0)
            builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    static{
        String diskData;
        try {
            diskData = new String(Files.readAllBytes(new File("data/output.html").toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            diskData = "";
        }
        htmlTemplate = diskData;
    }

    private String addSign(int mod) {
        return ((mod >= 0) ? "+" : "")+ mod;
    }
}
