package ui.controllers;

import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import model.data_managers.AllSpells;
import model.player.SpellManager;
import model.spells.Spell;
import ui.Main;

import java.util.Comparator;
import java.util.stream.Collectors;

public class SpellsTabController {

	@FXML
	private TreeView<String> allSpells, spellsKnown;

	@FXML
	private WebView spellDisplay;

	@FXML
	private TextField filter;

	@FXML
	private Label known0,known1,known2,known3,known4,known5,known6,known7,known8,known9,known10;
	private Label[] knowns;

	@FXML
	private void initialize() {
		knowns = new Label[]{known0, known1, known2, known3, known4, known5, known6, known7, known8, known9, known10};
		SpellManager spells = Main.character.spells();
		allSpells.setShowRoot(false);
		allSpells.setRoot(new TreeItem<>(""));
		spellsKnown.setShowRoot(false);
		spellsKnown.setRoot(new TreeItem<>(""));
		for(int i = 0; i <= 10; i++) {
			allSpells.getRoot().getChildren().add(new TreeItem<>(String.valueOf(i)));
			allSpells.getRoot().getChildren().get(i).getChildren().addAll(
					AllSpells.getSpells(i).stream().map(s->
							new TreeItem<>(s.getName())).collect(Collectors.toList()));
			allSpells.getRoot().getChildren().get(i).getChildren().sort(
					Comparator.comparing(TreeItem::getValue));
			spells.getSpellsKnown(i).addListener((ListChangeListener<Spell>) change -> {
				while(change.next()) {
					if(change.wasAdded()) {
						for (Spell spell : change.getAddedSubList()) {
							int j = spellsKnown.getRoot().getChildren().size();
							while(j <= spell.getLevel()) {
								spellsKnown.getRoot().getChildren().add(new TreeItem<>(String.valueOf(j)));
								j++;
							}
							spellsKnown.getRoot().getChildren().get(spell.getLevel())
									.getChildren().add(new TreeItem<>(spell.getName()));
						}
					}
					if(change.wasRemoved()) {
						for (Spell spell : change.getRemoved()) {
							spellsKnown.getRoot().getChildren().get(spell.getLevel())
									.getChildren().removeIf(o->o.getValue().equals(spell.getName()));
							int j = spellsKnown.getRoot().getChildren().size();
							while(j > 0 && spellsKnown.getRoot().getChildren().get(j-1).getChildren().size() == 0) {
								spellsKnown.getRoot().getChildren().remove(j-1);
								j--;
							}
						}
					}
				}
			});
		}
		ObservableMap<Integer, Integer> spellSlots = spells.getSpellSlots();
		spellSlots.addListener((MapChangeListener<Integer, Integer>) change ->
				knowns[change.getKey()].setText(String.valueOf(spellSlots.get(change.getKey()))));

		allSpells.setOnMouseClicked(event -> {
			if(allSpells.getSelectionModel().getSelectedItem() != null) {
				String item = allSpells.getSelectionModel().getSelectedItem().getValue();
				if (!item.matches("\\d{1,2}")) {
					if (event.getClickCount() == 2) {
						spells.addSpell(AllSpells.find(item));
					}
				}
			}
		});

		allSpells.getSelectionModel().selectedItemProperty().addListener(change->{
			if(allSpells.getSelectionModel().getSelectedItem() != null) {
				String item = allSpells.getSelectionModel().getSelectedItem().getValue();
				if (!item.matches("\\d{1,2}")) {
					renderSpell(AllSpells.find(item));
				}
			}
		});

		spellsKnown.setOnMouseClicked(event -> {
			if(allSpells.getSelectionModel().getSelectedItem() != null) {
				String item = spellsKnown.getSelectionModel().getSelectedItem().getValue();
				if (!item.matches("\\d{1,2}")) {
					Spell spell = AllSpells.find(item);
					if (event.getClickCount() == 2) {
						spells.removeSpell(spell);
					}
				}
			}
		});

		spellsKnown.getSelectionModel().selectedItemProperty().addListener(change->{
			if(allSpells.getSelectionModel().getSelectedItem() != null) {
				String item = spellsKnown.getSelectionModel().getSelectedItem().getValue();
				if (!item.matches("\\d{1,2}")) {
					renderSpell(AllSpells.find(item));
				}
			}
		});
	}

	private void renderSpell(Spell spell) {
		StringBuilder builder = new StringBuilder();
		builder.append("<p><b>").append(spell.getName())
				.append(" - Spell ").append(spell.getLevel())
				.append("</b><br><b>Traits</b> ").append(spell.getTraits().stream()
				.map(Enum::toString).collect(Collectors.joining(", ")))
				.append("<br><b>Traditions</b> ").append(spell.getTraditions().stream()
				.map(Enum::toString).collect(Collectors.joining(", ")))
				.append("</b><br><b>Cast</b> ");

		//Cast Time and Requirements
		if(!spell.getCastTime().equals(""))
			builder.append(spell.getCastTime().trim()).append(" (");
		builder.append(spell.getCast().stream()
				.map(Enum::toString).collect(Collectors.joining(", ")));
		if(!spell.getCastTime().equals(""))
			builder.append(")");
		if(!spell.getRequirements().equals(""))
			builder.append("; <b>Requirements</b> ").append(spell.getRequirements());

		//Range, Area, Targets
		builder.append("<br>");
		boolean semiColon = false;
		if(!spell.getRange().equals("")) {
			semiColon = true;
			builder.append("<b>Range</b> ").append(spell.getRange());
		}
		if(!spell.getArea().equals("")) {
			if(semiColon) builder.append("; ");
			else semiColon = true;
			builder.append("<b>Area</b> ").append(spell.getArea());
		}
		if(!spell.getTargets().equals("")) {
			if(semiColon) builder.append("; ");
			else semiColon = true;
			builder.append("<b>Targets</b> ").append(spell.getTargets());
		}

		//Saving Throws and Duration
		if(semiColon) builder.append("<br>");
		semiColon = false;
		if(!spell.getSave().equals("")) {
			semiColon = true;
			builder.append("<b>Saving Throw</b> ").append(spell.getSave());
		}
		if(!spell.getDuration().equals("")) {
			if(semiColon) builder.append("; ");
			builder.append("<b>Duration</b> ").append(spell.getDuration());
		}

		builder.append("<hr>").append(spell.getDescription().replaceAll("\n", "<br>")).append("</p");

		spellDisplay.getEngine().loadContent(builder.toString());
	}
}
