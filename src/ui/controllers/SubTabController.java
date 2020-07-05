package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public abstract class SubTabController {

    @FXML
    private TabPane tabPane;

    void sortTabs() {
        tabPane.getTabs().sort(Comparator.comparing(Tab::getText));
    }

    <T> void addTab(String name, Object controller) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(getTabPath()));
        fxmlLoader.setController(controller);
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

    abstract String getTabPath();

    void removeTab(String key) {
        Iterator<Tab> iterator = tabPane.getTabs().iterator();
        while(iterator.hasNext()) {
            Tab tab = iterator.next();
            if(tab.getText().equals(key)) {
                iterator.remove();
                return;
            }
        }

    }
}
