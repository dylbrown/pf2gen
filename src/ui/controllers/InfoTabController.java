package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.enums.Alignment;

import static ui.Main.character;

public class InfoTabController {
	@FXML
	private ComboBox<Alignment> alignment;
	@FXML
	private TextField characterName, playerName, height, weight, age, hair, eyes, gender;
	@FXML
	private Label level;
	@FXML
	private Button levelUp, levelDown;

	@FXML
	void initialize() {
		alignment.getItems().addAll(Alignment.values());
		alignment.getSelectionModel().selectedItemProperty()
				.addListener((o, oldValue, newValue)-> character.setAlignment(newValue));
		character.alignmentProperty().addListener(c ->
				alignment.getSelectionModel().select(character.getAlignment()));
		characterName.textProperty().bindBidirectional(character.qualities().getProperty("name"));
		playerName.textProperty().bindBidirectional(character.qualities().getProperty("player"));
		height.textProperty().bindBidirectional(character.qualities().getProperty("height"));
		weight.textProperty().bindBidirectional(character.qualities().getProperty("weight"));
		age.textProperty().bindBidirectional(character.qualities().getProperty("age"));
		eyes.textProperty().bindBidirectional(character.qualities().getProperty("eyes"));
		hair.textProperty().bindBidirectional(character.qualities().getProperty("hair"));
		gender.textProperty().bindBidirectional(character.qualities().getProperty("gender"));
		level.setText("0");
		character.levelProperty().addListener((event)-> level.setText(character.levelProperty().get().toString()));
		levelUp.setOnAction((event -> character.levelUp()));
		levelDown.setOnAction((event -> character.levelDown()));
	}
}
