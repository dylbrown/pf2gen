package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.ChoiceList;
import model.abilities.abilitySlots.FeatSlot;
import model.ability_scores.AbilityModChoice;
import model.data_managers.EquipmentManager;
import model.enums.Attribute;
import model.equipment.Equipment;
import model.xml_parsers.AncestriesLoader;
import model.xml_parsers.BackgroundsLoader;
import model.xml_parsers.ClassesLoader;
import ui.TemplateFiller;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static ui.Main.character;

public class Controller {
    @FXML
    private Button save;
    @FXML
    private Button load;
    @FXML
    private Button export;
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

        save.setOnAction((event -> save()));
        load.setOnAction((event -> load()));
    }

    private void load() {
        FileChooser fileChooser = new FileChooser();
        if(!Objects.equals(characterName.getText(), ""))
            fileChooser.setInitialFileName(characterName.getText().replaceAll(" ",""));
        fileChooser.setInitialDirectory(new File("./"));
        //Set extension filter for text files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PF2 files", "*.pf2")
        );

        //Show save file dialog
        File file = fileChooser.showOpenDialog(export.getScene().getWindow());
        Map<String, Object> map;
        if (file != null) {
            long start = System.currentTimeMillis();
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                map = (Map<String, Object>) in.readObject();
                in.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                map = new HashMap<>();
            }
            //Name
            character.setName((String) map.get("name"));
            //Ancestry
            String anc = (String) map.get("ancestry");
            for (Ancestry ancestry : new AncestriesLoader().parse()) {
                if(ancestry.getName().equals(anc)){
                    character.setAncestry(ancestry);
                    break;
                }
            }
            //Background
            String bac = (String) map.get("background");
            for (Background background : new BackgroundsLoader().parse()) {
                if(background.getName().equals(bac)){
                    character.setBackground(background);
                    break;
                }
            }
            //PClass
            String cla = (String) map.get("class");
            for (PClass pClass : new ClassesLoader().parse()) {
                if(pClass.getName().equals(cla)){
                    character.setClass(pClass);
                    break;
                }
            }
            //Level
            Integer lvl = (Integer) map.get("level");
            while(character.getLevel() < lvl)
                character.levelUp();
            //Ability Score Choices
            List<AbilityModChoice> mods = new ArrayList<>((List<AbilityModChoice>) map.get("abilityChoices"));
            for (AbilityModChoice modChoice : character.getAbilityScoreChoices()) {
                Iterator<AbilityModChoice> iterator = mods.iterator();
                while(iterator.hasNext()){
                    AbilityModChoice next = iterator.next();
                    if(next.matches(modChoice)){
                        character.choose(modChoice, next.getTarget());
                        iterator.remove();
                        break;
                    }
                }
            }
            //AbilitySlot Decisions
            List<String> decisionStrings = (List<String>) map.get("decisions");
            Map<String, String> decisionStringMap = new HashMap<>();
            for (String s : decisionStrings) {
                String[] split = s.split(";");
                decisionStringMap.put(split[0], split[1]);
            }
            for (Choice decision : character.decisions().getDecisions()) {
                String choice = decisionStringMap.get(decision.toString());
                if(choice != null){
                    List options;
                    if(decision instanceof ChoiceList)
                        options = ((ChoiceList) decision).getOptions();
                    else if(decision instanceof FeatSlot)
                        options = character.abilities().getOptions((FeatSlot)decision);
                    else options = Collections.emptyList();
                    for (Object option : options) {
                        if(option.toString().equals(choice)){
                            character.choose(decision, option);
                            break;
                        }
                    }
                }
            }
            character.attributes().resetSkills();
            //Skill Increase Choices
            SortedMap<Integer, Set<Attribute>> skillChoices = (SortedMap<Integer, Set<Attribute>>) map.get("skillChoices");
            for (Set<Attribute> choices : skillChoices.values()) {
                for (Attribute choice : choices) {
                        character.attributes().advanceSkill(choice);
                }
            }

            //Items
            character.inventory().reset();
            HashMap<String, Integer> items = (HashMap<String, Integer>) map.get("inventory");
            for (Equipment equipment : EquipmentManager.getEquipment()) {
                if (items.get(equipment.getName()) != null)
                    character.inventory().buy(equipment, items.get(equipment.getName()));
            }

            System.out.println(System.currentTimeMillis()-start+" ms");
        }
    }

    private void save() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", character.getName());
        map.put("ancestry", character.getAncestry().getName());
        map.put("background", character.getBackground().getName());
        map.put("class", character.getPClass().getName());
        map.put("level", character.getLevel());
        map.put("abilityChoices", character.getAbilityScoreChoices());
        map.put("decisions", character.decisions().getDecisions().stream().filter(
                choice -> choice.getChoice()!=null).map(
                (choice -> choice.toString()+";"+choice.getChoice().toString())).collect(
                        Collectors.toList()));
        map.put("skillChoices", character.attributes().getSkillChoices());
        HashMap<String, Integer> items = new HashMap<>();
        character.inventory().getItems().values().forEach((item)-> items.put(item.getName(), item.getCount()));
        map.put("inventory", items);

        FileChooser fileChooser = new FileChooser();
        if(!Objects.equals(characterName.getText(), ""))
            fileChooser.setInitialFileName(characterName.getText().replaceAll(" ",""));
        fileChooser.setInitialDirectory(new File("./"));
        //Set extension filter for text files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PF2 files", "*.pf2")
        );

        //Show save file dialog
        File file = fileChooser.showSaveDialog(export.getScene().getWindow());

        if (file != null) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
                out.writeObject(map);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
