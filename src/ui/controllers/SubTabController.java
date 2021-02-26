package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class SubTabController {

    @FXML
    private TabPane tabPane;
    private final Map<String, Tab> tabs = new HashMap<>();

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
                tabs.put(name, tab);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void navigateToTab(String name) {
        Tab tab = tabs.get(name);
        if(tab != null)
            tabPane.getSelectionModel().select(tab);
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
