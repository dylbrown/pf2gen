package ui.controllers;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import model.data_managers.sources.SourcesLoader;
import setting.Deity;
import ui.Main;
import ui.html.ABCHTMLGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class StartingTabController {

    @FXML
    private TabPane tabPane;

    @FXML
    private void initialize() {
        addTab("Ancestry", FXCollections.unmodifiableObservableList(
                    FXCollections.observableList(new ArrayList<>(SourcesLoader.instance()
                            .ancestries().getAll().values()))
                ),
                Main.character.getAncestryProperty(),
                Main.character::setAncestry,
                ABCHTMLGenerator::parseAncestry);
        addTab("Background", FXCollections.unmodifiableObservableList(
                FXCollections.observableList(new ArrayList<>(SourcesLoader.instance()
                        .backgrounds().getAll().values()))
                ),
                Main.character.getBackgroundProperty(),
                Main.character::setBackground,
                ABCHTMLGenerator::parseBackground);
        addTab("Class", FXCollections.unmodifiableObservableList(
                FXCollections.observableList(new ArrayList<>(SourcesLoader.instance()
                        .classes().getAll().values()))
                ),
                Main.character.getPClassProperty(),
                Main.character::setPClass,
                ABCHTMLGenerator::parsePClass);
        ObservableList<Deity> deities = FXCollections.observableArrayList();
        deities.add(Deity.NO_DEITY);
        deities.addAll(SourcesLoader.instance()
                .deities().getAll().values());
        addTab("Deity", FXCollections.unmodifiableObservableList(
                deities
                ),
                Main.character.getDeityProperty(),
                Main.character::setDeity,
                ABCHTMLGenerator::parseDeity);
    }

    private <T> void addTab(String name, ObservableList<T> options, ReadOnlyObjectProperty<T> value,
                            Consumer<T> adder, Function<T, String> htmlGenerator) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/singleChoicePage.fxml"));
        fxmlLoader.setController(new SingleChoicePageController<>(options, value, adder, htmlGenerator));
        try {
            Object load = fxmlLoader.load();
            if(load instanceof Node) {
                Tab tab = new Tab(name, (Node) load);
                tab.setClosable(false);
                tabPane.getTabs().add(tab);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
