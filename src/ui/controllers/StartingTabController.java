package ui.controllers;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import model.data_managers.sources.SourcesLoader;
import setting.Deity;
import ui.Main;
import ui.html.ABCHTMLGenerator;
import ui.html.SettingHTMLGenerator;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class StartingTabController extends SubTabController {

    @FXML
    private void initialize() {
        addTab("Ancestry", FXCollections.unmodifiableObservableList(
                    FXCollections.observableList(new ArrayList<>(SourcesLoader.instance()
                            .ancestries().getAll().values()))
                ),
                Main.character.getAncestryProperty(),
                Main.character::setAncestry,
                ABCHTMLGenerator::parse);
        addTab("Background", FXCollections.unmodifiableObservableList(
                FXCollections.observableList(new ArrayList<>(SourcesLoader.instance()
                        .backgrounds().getAll().values()))
                ),
                Main.character.getBackgroundProperty(),
                Main.character::setBackground,
                ABCHTMLGenerator::parse);
        addTab("Class", FXCollections.unmodifiableObservableList(
                FXCollections.observableList(new ArrayList<>(SourcesLoader.instance()
                        .classes().getAll().values()))
                ),
                Main.character.getPClassProperty(),
                Main.character::setPClass,
                ABCHTMLGenerator::parse);
        ObservableList<Deity> deities = FXCollections.observableArrayList();
        deities.add(Deity.NO_DEITY);
        deities.addAll(SourcesLoader.instance()
                .deities().getAll().values());
        addTab("Deity", FXCollections.unmodifiableObservableList(
                deities
                ),
                Main.character.getDeityProperty(),
                Main.character::setDeity,
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
}
