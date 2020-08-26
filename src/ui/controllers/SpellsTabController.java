package ui.controllers;

import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import model.CharacterManager;
import model.spells.SpellList;

public class SpellsTabController extends SubTabController {

	@FXML
	private void initialize() {
		CharacterManager.getActive().spells().getSpellLists().addListener((MapChangeListener<String, SpellList>) c->{
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
