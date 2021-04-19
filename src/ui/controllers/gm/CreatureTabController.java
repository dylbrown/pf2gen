package ui.controllers.gm;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.creatures.CustomAttack;
import model.creatures.CustomCreatureCreator;
import model.creatures.CustomCreatureValue;
import model.creatures.scaling.ScaleMap;
import model.data_managers.sources.SourcesLoader;
import model.enums.Alignment;
import model.enums.Size;
import ui.controls.lists.entries.CreatureChoiceCell;

public class CreatureTabController {

    @FXML
    private TextField name;
    @FXML
    private Spinner<Integer> level;
    @FXML
    private ComboBox<Alignment> alignment;
    @FXML
    private ComboBox<Size> size;
    @FXML
    private ListView<CustomCreatureValue<Attribute>> coreAttributes, skills, spells;
    @FXML
    private ListView<CustomCreatureValue<AbilityScore>> abilityScores;
    @FXML
    private Button traitsButton;
    @FXML
    private Button languagesButton;
    @FXML
    private Button weakResistButton;
    @FXML
    private ListView<CustomAttack> strikesList;
    @FXML
    private Button addStrike;
    @FXML
    private void initialize(){
        CustomCreatureCreator creatureCreator = new CustomCreatureCreator(SourcesLoader.ALL_SOURCES);
        creatureCreator.name.bind(name.textProperty());

        level.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 30));
        creatureCreator.level.bind(level.valueProperty());

        alignment.getItems().addAll(Alignment.values());
        creatureCreator.alignment.bind(alignment.valueProperty());

        size.getItems().addAll(Size.values());
        creatureCreator.size.bind(size.valueProperty());

        abilityScores.setItems(creatureCreator.getAbilityScores());

        abilityScores.setCellFactory(listView -> new CreatureChoiceCell<>(creatureCreator.level,
                ScaleMap.ABILITY_MODIFIER_SCALES));
        //coreAttributes.setCellFactory(listView -> new CreatureChoiceCell<>());
        //skills.setCellFactory(listView -> new CreatureChoiceCell<>());
        //spells.setCellFactory(listView -> new CreatureChoiceCell<>());
    }
}
