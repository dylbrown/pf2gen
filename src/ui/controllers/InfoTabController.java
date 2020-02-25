package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import model.data_managers.SaveLoadManager;
import model.enums.Alignment;

import java.io.File;
import java.util.Objects;

import static ui.Main.character;

public class InfoTabController {
	@FXML
	private ComboBox<Alignment> alignment;
	@FXML
	private Button save;
	@FXML
	private Button load;
	@FXML
	private TextField characterName, playerName, height, weight, age, hair, eyes, gender;
	@FXML
	private Label level;
	@FXML
	private Button levelUp, levelDown;

	@FXML
	void initialize() {
		alignment.getItems().addAll(Alignment.values());
		alignment.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue)-> character.setAlignment(newValue));
		characterName.textProperty().addListener((observable, oldValue, newValue) -> character.setName(newValue));
		playerName.textProperty().addListener((observable, oldValue, newValue) -> character.setPlayer(newValue));
		height.textProperty().addListener((observable, oldValue, newValue) -> character.setHeight(newValue));
		weight.textProperty().addListener((observable, oldValue, newValue) -> character.setWeight(newValue));
		age.textProperty().addListener((observable, oldValue, newValue) -> character.setAge(newValue));
		eyes.textProperty().addListener((observable, oldValue, newValue) -> character.setEyes(newValue));
		hair.textProperty().addListener((observable, oldValue, newValue) -> character.setHair(newValue));
		gender.textProperty().addListener((observable, oldValue, newValue) -> character.setGender(newValue));
		level.setText("0");
		character.getLevelProperty().addListener((event)-> level.setText(character.getLevelProperty().get().toString()));
		levelUp.setOnAction((event -> character.levelUp()));
		levelDown.setOnAction((event -> character.levelDown()));

		save.setOnAction((event -> save()));
		load.setOnAction((event -> load()));
	}

	private File loadLocation = new File("./");
	private void load() {
		FileChooser fileChooser = new FileChooser();
		if(!Objects.equals(characterName.getText(), ""))
			fileChooser.setInitialFileName(characterName.getText().replaceAll(" ",""));
		fileChooser.setInitialDirectory(loadLocation);
		//Set extension filter for text files
		fileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("PF2 files", "*.pf2")
		);

		//Show save file dialog
		File file = fileChooser.showOpenDialog(load.getScene().getWindow());
		loadLocation = file.getParentFile();
		SaveLoadManager.load(file);

		characterName.setText(character.getName());
		playerName.setText(character.getPlayer());
		weight.setText(character.getWeight());
		height.setText(character.getHeight());
		age.setText(character.getAge());
		hair.setText(character.getHair());
		eyes.setText(character.getEyes());
		gender.setText(character.getGender());
		alignment.getSelectionModel().select(character.getAlignment());
		level.setText(String.valueOf(character.getLevel()));
	}

	private File saveLocation = new File("./");
	private void save() {
		FileChooser fileChooser = new FileChooser();
		if(!Objects.equals(characterName.getText(), ""))
			fileChooser.setInitialFileName(characterName.getText().replaceAll(" ",""));
		fileChooser.setInitialDirectory(saveLocation);
		//Set extension filter for text files
		fileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("PF2 files", "*.pf2")
		);

		//Show save file dialog
		File file = fileChooser.showSaveDialog(save.getScene().getWindow());
		saveLocation = file.getParentFile();
		SaveLoadManager.save(file);
	}
}
