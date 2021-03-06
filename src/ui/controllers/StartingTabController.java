package ui.controllers;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import model.CharacterManager;
import model.player.PC;
import model.setting.Deity;
import ui.html.ABCHTMLGenerator;
import ui.html.SettingHTMLGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class StartingTabController extends SubTabController {

    private PC character;

    @FXML
    private void initialize() {
        character = CharacterManager.getActive();
        addTab("Ancestry", FXCollections.unmodifiableObservableList(
                    FXCollections.observableList(new ArrayList<>(character.sources()
                            .ancestries().getAll().values()))
                ),
                character.ancestryProperty(),
                character::setAncestry,
                ABCHTMLGenerator::parse);
        addTab("Background", FXCollections.unmodifiableObservableList(
                FXCollections.observableList(new ArrayList<>(character.sources()
                        .backgrounds().getAll().values()))
                ),
                character.backgroundProperty(),
                character::setBackground,
                ABCHTMLGenerator::parse);
        addTab("Class", FXCollections.unmodifiableObservableList(
                FXCollections.observableList(new ArrayList<>(character.sources()
                        .classes().getAll().values()))
                ),
                character.pClassProperty(),
                character::setPClass,
                ABCHTMLGenerator::parse);
        ObservableList<Deity> deities = FXCollections.observableArrayList();
        deities.addAll(character.sources()
                .deities().getAll().values());
        addTab("Deity", FXCollections.unmodifiableObservableList(
                deities
                ),
                character.deityProperty(),
                character::setDeity,
                SettingHTMLGenerator::parse);
    }

    <T> void addTab(String name, ObservableList<T> options, ReadOnlyObjectProperty<T> value,
                    Consumer<T> adder, Function<T, String> htmlGenerator) {
        addTab(name, new SingleChoicePageController<>(options, value, adder, htmlGenerator));
    }

    @Override
    String getTabPath() {
        return "/fxml/singleChoicePage.fxml";
    }

    public void navigate(List<String> path) {
        navigateToTab(path.get(0));
    }
}
