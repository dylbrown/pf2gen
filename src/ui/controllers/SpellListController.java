package ui.controllers;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import model.CharacterManager;
import model.player.PC;
import model.spells.Spell;
import model.spells.SpellList;
import model.spells.Tradition;
import model.util.ObjectNotFoundException;
import ui.html.SpellHTMLGenerator;

import java.util.Comparator;
import java.util.stream.Collectors;

public class SpellListController {
    @FXML
    private TreeView<String> allSpells, spellsKnown;

    private final ListView<String> filterList = new ListView<>();

    @FXML
    private BorderPane spellsContainer;

    @FXML
    private WebView spellDisplay;

    @FXML
    private TextField filter;

    @FXML
    private Label known0,known1,known2,known3,known4,known5,known6,known7,known8,known9,known10;
    @FXML
    private Label slots0, slots1, slots2, slots3, slots4, slots5, slots6, slots7, slots8, slots9, slots10;
    private Label[] slots, knowns;
    private final SpellList spells;
    private PC character;

    SpellListController(SpellList spells) {
        this.spells = spells;
    }

    @FXML
    private void initialize() {
        this.character = CharacterManager.getActive();
        slots = new Label[]{slots0, slots1, slots2, slots3, slots4, slots5, slots6, slots7, slots8, slots9, slots10};
        knowns = new Label[]{known0, known1, known2, known3, known4, known5, known6, known7, known8, known9, known10};
        allSpells.setShowRoot(false);
        allSpells.setRoot(new TreeItem<>(""));
        spellsKnown.setShowRoot(false);
        spellsKnown.setRoot(new TreeItem<>(""));
        showAllSpells(spells.getTradition().get());
        spells.getTradition().addListener((observable, oldValue, newValue) -> showAllSpells(newValue));
        for(int i = 0; i <= 10; i++) {
            spells.getSpellsKnown(i).addListener((ListChangeListener<Spell>) change -> {
                while(change.next()) {
                    if(change.wasAdded()) {
                        for (Spell spell : change.getAddedSubList()) {
                            int j = spellsKnown.getRoot().getChildren().size();
                            while(j <= spell.getLevelOrCantrip()) {
                                spellsKnown.getRoot().getChildren().add(new TreeItem<>(String.valueOf(j)));
                                j++;
                            }
                            spellsKnown.getRoot().getChildren().get(spell.getLevelOrCantrip())
                                    .getChildren().add(new TreeItem<>(spell.getName()));
                            spellsKnown.getRoot().getChildren().get(spell.getLevelOrCantrip())
                                    .getChildren().sort(Comparator.comparing(TreeItem::getValue));
                        }
                    }
                    if(change.wasRemoved()) {
                        for (Spell spell : change.getRemoved()) {
                            spellsKnown.getRoot().getChildren().get(spell.getLevelOrCantrip())
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
        ObservableList<Integer> spellSlots = spells.getSpellSlots();
        ObservableList<Integer> extraSpellsKnown = spells.getExtraSpellsKnown();
        spellSlots.addListener((ListChangeListener<Integer>) c->{
            while(c.next()) {
                if (c.wasReplaced()) {
                    for (int i = c.getFrom() ; i < c.getTo() ; i++) {
                        slots[i].setText(String.valueOf(spellSlots.get(i)));
                        knowns[i].setText(String.valueOf(spellSlots.get(i) + extraSpellsKnown.get(i)));
                    }
                }
            }
        });
        extraSpellsKnown.addListener((ListChangeListener<Integer>) c->{
            while(c.next()) {
                if (c.wasReplaced()) {
                    for (int i = c.getFrom() ; i < c.getTo() ; i++) {
                        knowns[i].setText(String.valueOf(spellSlots.get(i) + extraSpellsKnown.get(i)));
                    }
                }
            }
        });

        allSpells.setOnMouseClicked(event -> {
            if(allSpells.getSelectionModel().getSelectedItem() != null) {
                String item = allSpells.getSelectionModel().getSelectedItem().getValue();
                if (!item.matches("\\d{1,2}")) {
                    if (event.getClickCount() == 2) {
                        try {
                            spells.addSpell(character.sources().spells().find(item));
                        } catch (ObjectNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        allSpells.getSelectionModel().selectedItemProperty().addListener(change->{
            if(allSpells.getSelectionModel().getSelectedItem() != null) {
                String item = allSpells.getSelectionModel().getSelectedItem().getValue();
                if (!item.matches("\\d{1,2}")) {
                    try {
                        renderSpell(character.sources().spells().find(item));
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        spellsKnown.getSelectionModel().selectedItemProperty().addListener(change->{
            if(spellsKnown.getSelectionModel().getSelectedItem() != null) {
                String item = spellsKnown.getSelectionModel().getSelectedItem().getValue();
                if (!item.matches("\\d{1,2}")) {
                    try {
                        renderSpell(character.sources().spells().find(item));
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        spellsKnown.setOnMouseClicked(event -> {
            if(spellsKnown.getSelectionModel().getSelectedItem() != null) {
                String item = spellsKnown.getSelectionModel().getSelectedItem().getValue();
                if (!item.matches("\\d{1,2}")) {
                    Spell spell = null;
                    try {
                        spell = character.sources().spells().find(item);
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
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
                    try {
                        renderSpell(character.sources().spells().find(item));
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        filter.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.equals(oldValue)){
                if(!newValue.equals("")) {
                    filterList.getItems().setAll(
                            character.sources().spells().getAll().values().stream()
                                    .map(Spell::getName)
                                    .filter(s -> s.toLowerCase().contains(newValue.toLowerCase()))
                                    .collect(Collectors.toList()));
                    spellsContainer.setCenter(filterList);
                }else{
                    spellsContainer.setCenter(allSpells);
                }
            }
        });
    }

    private void showAllSpells(Tradition tradition) {
        allSpells.getRoot().getChildren().clear();
        for(int i=0; i <= 10; i++) {
            allSpells.getRoot().getChildren().add(new TreeItem<>(String.valueOf(i)));
            allSpells.getRoot().getChildren().get(i).getChildren().addAll(
                    character.sources().spells().getSpells(tradition, i).stream().map(s->
                            new TreeItem<>(s.getName())).collect(Collectors.toList()));
            allSpells.getRoot().getChildren().get(i).getChildren().sort(
                    Comparator.comparing(TreeItem::getValue));
        }
    }

    private void renderSpell(Spell spell) {
        int level = spell.getLevelOrCantrip();
        if(level == 0)
            level = spells.getHighestLevelCanCast();
        spellDisplay.getEngine().loadContent(SpellHTMLGenerator.parse(spell, level));
    }
}
