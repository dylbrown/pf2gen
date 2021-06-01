package ui.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.ability_slots.Choice;
import model.ability_slots.ChoiceList;
import model.util.ObservableUtils;
import ui.controllers.util.ChoicePageController;
import ui.controls.Popup;

import java.io.IOException;
import java.util.function.Function;

public class ChoicePopupController<T> implements Popup.Controller {
    private final ChoicePageController<T> controller;
    private Stage stage = null;
    @FXML
    private BorderPane pane;
    @FXML
    private Button doneButton;

    public ChoicePopupController(ObservableList<T> options, Choice<T> choice, Function<T, String> htmlGenerator) {
        this.controller = new ChoicePageController<>(options, choice, htmlGenerator);
    }

    public ChoicePopupController(ChoiceList<T> choice, Function<T, String> htmlGenerator) {
        this(ObservableUtils.makeList(choice.getOptions()), choice, htmlGenerator);
    }

    @FXML
    private void initialize() {
        FXMLLoader loader = new FXMLLoader(controller.getClass().getResource("/fxml/choicePage.fxml"));
        loader.setController(controller);
        try {
            pane.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        doneButton.setOnAction(a->stage.close());
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
