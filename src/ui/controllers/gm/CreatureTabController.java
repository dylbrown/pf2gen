package ui.controllers.gm;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.creatures.CustomAttack;
import model.creatures.CustomCreatureAttribute;
import model.enums.Alignment;
import model.enums.Size;

public class CreatureTabController {

    @FXML
    private ListView<CustomCreatureAttribute> abilityScores, coreAttributes, skills, spells;
    @FXML
    private TextField name;
    @FXML
    private Spinner<Integer> level;
    @FXML
    private ComboBox<Alignment> alignment;
    @FXML
    private ComboBox<Size> size;
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
    private ListView<CustomCreatureAttribute> attributesList;
    @FXML
    private void initialize(){

    }
}
