package ui.controllers.gm;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.attributes.BaseAttribute;
import model.creatures.CustomStrike;
import model.creatures.CustomCreatureCreator;
import model.creatures.CustomCreatureValue;
import model.data_managers.sources.SourcesLoader;
import model.enums.Alignment;
import model.enums.Size;
import model.enums.Trait;
import ui.controllers.ChoicePopupController;
import ui.controls.Popup;
import ui.controls.lists.entries.CreatureChoiceCell;
import ui.controls.lists.entries.CustomStrikeCell;
import ui.html.HTMLGenerator;

import java.util.Arrays;

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
    private Button traitsButton, languagesButton, weakResistButton, skillsButton;
    @FXML
    private ListView<CustomStrike> strikesList;
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
        coreAttributes.getItems().addAll(Arrays.asList(
                creatureCreator.getOrCreateModifier(BaseAttribute.Fortitude),
                creatureCreator.getOrCreateModifier(BaseAttribute.Reflex),
                creatureCreator.getOrCreateModifier(BaseAttribute.Will),
                creatureCreator.getOrCreateModifier(BaseAttribute.Perception)
        ));

        traitsButton.setOnAction(e-> Popup.popup("/fxml/choicePagePopup.fxml",
                new ChoicePopupController<>(creatureCreator.getTraitsChoice(),
                        HTMLGenerator.getGenerator(Trait.class))));

        languagesButton.setOnAction(e-> Popup.popup("/fxml/choicePagePopup.fxml",
                new ChoicePopupController<>(creatureCreator.getLanguagesChoice(), (l)->"")));

        skillsButton.setOnAction(e-> Popup.popup("/fxml/choicePagePopup.fxml",
                new ChoicePopupController<>(creatureCreator.getSkillsChoice(), (a)->"")));

        skills.setItems(creatureCreator.getSkills());

        addStrike.setOnAction(e->creatureCreator.getStrikes().add(new CustomStrike(creatureCreator.level)));

        strikesList.setItems(creatureCreator.getStrikes());

        abilityScores.setCellFactory(listView -> new CreatureChoiceCell<>(creatureCreator.level));
        coreAttributes.setCellFactory(listView -> new CreatureChoiceCell<>(creatureCreator.level));
        skills.setCellFactory(listView -> new CreatureChoiceCell<>(creatureCreator.level));
        //spells.setCellFactory(listView -> new CreatureChoiceCell<>());
        strikesList.setCellFactory(listView -> new CustomStrikeCell(creatureCreator.level, s->strikesList.getItems().remove(s)));
    }
}
