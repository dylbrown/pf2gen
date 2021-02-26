package ui.controllers;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import model.CharacterManager;
import model.spells.CasterType;
import model.spells.Spell;
import model.spells.SpellList;
import model.spells.Tradition;
import model.util.FlattenedList;
import ui.controls.lists.ObservableEntryList;
import ui.controls.lists.entries.SpellEntry;
import ui.controls.lists.factories.TreeCellFactory;
import ui.html.SpellHTMLGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class SpellListController {
    private ObservableEntryList<Spell, SpellEntry> allSpells, spellsKnown;

    @FXML
    private AnchorPane spellsKnownContainer;

    @FXML
    private BorderPane spellsContainer;

    @FXML
    private WebView spellDisplay;

    @FXML
    private TextField filter;
    private final ReadOnlyObjectWrapper<Predicate<SpellEntry>> filterPredicate =
            new ReadOnlyObjectWrapper<>(null);

    @FXML
    private Label known0,known1,known2,known3,known4,known5,known6,known7,known8,known9,known10;
    @FXML
    private Label slots0, slots1, slots2, slots3, slots4, slots5, slots6, slots7, slots8, slots9, slots10;
    private Label[] slots, knowns;
    private final SpellList spells;
    private final ObservableList<Spell> allSpellsList = FXCollections.observableArrayList();

    SpellListController(SpellList spells) {
        this.spells = spells;
    }

    @FXML
    private void initialize() {
        slots = new Label[]{slots0, slots1, slots2, slots3, slots4, slots5, slots6, slots7, slots8, slots9, slots10};
        knowns = new Label[]{known0, known1, known2, known3, known4, known5, known6, known7, known8, known9, known10};
        allSpells = ObservableEntryList.makeList(allSpellsList,
                (spell, clickCount) -> {
                    renderSpell(spell);
                    if (clickCount == 2) {
                        spells.addSpell(spell);
                    }
                },
                spell -> "Level " + spell.getLevelOrCantrip(),
                SpellEntry::new,
                SpellEntry::new,
                this::makeColumns);
        allSpells.setFilter(filterPredicate.getReadOnlyProperty());
        spellsKnown = ObservableEntryList.makeList(new FlattenedList<>(spells.getSpellsKnown()),
                (spell, clickCount) -> {
                    renderSpell(spell);
                    if (clickCount == 2) {
                        spells.removeSpell(spell);
                    }
                },
                spell -> String.valueOf(spell.getLevelOrCantrip()),
                SpellEntry::new,
                SpellEntry::new,
                this::makeColumns);

        setAllSpells(spells.getTradition().get());
        spells.getTradition().addListener((observable, oldValue, newValue) -> setAllSpells(newValue));

        // Insert Custom Containers into template
        spellsContainer.setCenter(allSpells);
        spellsKnownContainer.getChildren().setAll(spellsKnown);
        AnchorPane.setTopAnchor(spellsKnown, 0.0);
        AnchorPane.setBottomAnchor(spellsKnown, 0.0);
        AnchorPane.setLeftAnchor(spellsKnown, 0.0);
        AnchorPane.setRightAnchor(spellsKnown, 0.0);

        ObservableList<Integer> spellSlots = spells.getSpellSlots();
        ObservableList<Integer> extraSpellsKnown = spells.getExtraSpellsKnown();
        spellSlots.addListener((ListChangeListener<Integer>) c->{
            while(c.next()) {
                if (c.wasReplaced()) {
                    updateRange(c.getFrom(), c.getTo());
                }
            }
        });
        extraSpellsKnown.addListener((ListChangeListener<Integer>) c->{
            while(c.next()) {
                if (c.wasReplaced()) {
                    updateRange(c.getFrom(), c.getTo());
                }
            }
        });
        updateRange(0, 11);

        spellsKnown.getSelectionModel().selectedItemProperty().addListener(change->{
            if(allSpells.getSelectionModel().getSelectedItem() != null) {
                SpellEntry item = allSpells.getSelectionModel().getSelectedItem().getValue();
                if (item.getContents() != null) {
                    renderSpell(item.getContents());
                }
            }
        });

        filter.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.equals(oldValue)){
                filterPredicate.set(spellEntry -> {
                    Spell spell = spellEntry.getContents();
                    return spell != null && spell.getName().toLowerCase().contains(newValue);
                });
            }
        });
    }

    private void setAllSpells(Tradition tradition) {
        List<Spell> list;
        boolean update = !allSpellsList.isEmpty();
        if(!update)
            list = allSpellsList;
        else
            list = new ArrayList<>();
        for(int i = 0; i <= 10; i++) {
            list.addAll(CharacterManager.getActive().sources().spells().getSpells(tradition, i));
            if(spells.getCasterType().get().equals(CasterType.Spontaneous))
                list.addAll(CharacterManager.getActive().sources().spells().getHeightenedSpells(tradition, i));
        }
        if(update)
            allSpellsList.setAll(list);
    }

    private List<TreeTableColumn<SpellEntry, ?>> makeColumns(ReadOnlyDoubleProperty width) {
        TreeTableColumn<SpellEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<SpellEntry, String> school = new TreeTableColumn<>("School");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        // name.minWidthProperty().bind(width.multiply(.6));
        name.setComparator((s1, s2)->{
            if(s1.matches("\\ALevel \\d{1,2}\\z") && s2.matches("\\ALevel \\d{1,2}\\z")) {
                int i1 = Integer.parseInt(s1.substring("Level ".length()));
                int i2 = Integer.parseInt(s2.substring("Level ".length()));
                return Integer.compare(i1, i2);
            }
            return s1.compareTo(s2);
        });
        school.setCellValueFactory(new TreeCellFactory<>("school"));
        school.setStyle( "-fx-alignment: CENTER;");
        return Arrays.asList(name, school);
    }

    private void updateRange(int from, int to) {
        for (int i = from ; i < to ; i++) {
            slots[i].setText(String.valueOf(spells.getSpellSlots().get(i)));
            knowns[i].setText(String.valueOf(spells.getSpellSlots().get(i) + spells.getExtraSpellsKnown().get(i)));
            knowns[i].setText(String.valueOf(spells.getSpellSlots().get(i) + spells.getExtraSpellsKnown().get(i)));
        }
    }

    private void renderSpell(Spell spell) {
        int level = spell.getLevelOrCantrip();
        if(level == 0)
            level = spells.getHighestLevelCanCast();
        spellDisplay.getEngine().loadContent(SpellHTMLGenerator.parse(spell, level));
    }
}
