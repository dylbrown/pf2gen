package ui.controllers;

import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import model.spells.SpellList;
import ui.Main;

public class SpellsTabController extends SubTabController {

	@FXML
	private void initialize() {
		Main.character.spells().getSpellLists().addListener((MapChangeListener<String, SpellList>) c->{
			if(c.wasAdded()) {
				addTab(c.getKey(), new SpellListController(c.getValueAdded()));
				sortTabs();
			}
			if(c.wasRemoved()) {
				removeTab(c.getKey());
			}
		});
	}

	@Override
	String getTabPath() {
		return "/fxml/spellListPage.fxml";
	}
}
