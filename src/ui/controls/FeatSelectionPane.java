package ui.controls;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.web.WebView;
import model.abilities.Ability;
import model.ability_slots.FeatSlot;
import model.ability_slots.SingleChoice;
import model.enums.Type;
import model.player.AbilityManager;
import ui.controls.lists.AbilityList;
import ui.html.AbilityHTMLGenerator;

import java.util.ArrayList;
import java.util.List;

import static ui.Main.character;

public class FeatSelectionPane extends SelectionPane<Ability> {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Ability> unmetPrereqs = new ArrayList<>();
    private final ObservableList<Ability> allItems = FXCollections.observableArrayList();
    private final AbilityList itemsList, allItemsList;

    public FeatSelectionPane(SingleChoice<Ability> slot, WebView display, ToggleGroup filterChoices) {
        this.display = display;
        AbilityManager abilities = character.abilities();
        init(slot);
        allItems.addAll(abilities.getOptions(slot));
        items.addAll(abilities.getOptions(slot));
        items.removeIf((item)->{
            if(!character.meetsPrerequisites(item)){
                unmetPrereqs.add(item);
                return true;
            }
            return false;
        });
        items.removeAll(slot.getSelections());
        unmetPrereqs.removeAll(slot.getSelections());
        allItems.removeAll(slot.getSelections());
        slot.getSelections().addListener((ListChangeListener<Ability>) c->{
            while(c.next()) {
                allItems.removeAll(c.getAddedSubList());
                allItems.addAll(c.getRemoved());
                items.removeAll(c.getAddedSubList());
                unmetPrereqs.removeAll(c.getAddedSubList());
                for (Ability ability : c.getRemoved()) {
                    if(!character.meetsPrerequisites(ability)) {
                        unmetPrereqs.add(ability);
                    }
                }

            }
        });
        abilities.getAbilities().addListener((ListChangeListener<Ability>) (event)->{
            while(event.next()) {
                if (event.wasAdded()) {
                    unmetPrereqs.removeIf((item) -> {
                        if (character.meetsPrerequisites(item)) {
                            if(!items.contains(item))
                                items.add(item);
                            return true;
                        }
                        return false;
                    });
                }
                if (event.wasRemoved()) {
                    unmetPrereqs.removeAll(event.getRemoved());
                    items.removeIf((item)->{
                        if(!character.meetsPrerequisites(item)){
                            unmetPrereqs.add(item);
                            return true;
                        }
                        return false;
                    });
                    for (Ability item : event.getRemoved()) {
                        if(abilities.getOptions(slot).contains(item)){
                            if (!character.meetsPrerequisites(item)) {
                                unmetPrereqs.add(item);
                            }else if(!items.contains(item))
                                items.add(item);
                        }
                    }
                }
            }
        });
        if(slot instanceof FeatSlot) {
            if(((FeatSlot) slot).getAllowedTypes().contains(Type.Ancestry.toString())
                    || ((FeatSlot) slot).getAllowedTypes().contains(Type.Heritage.toString())){
                character.addAncestryObserver((observable) -> items.setAll(abilities.getOptions(slot)));
            }
        }
        filterChoices.selectedToggleProperty().addListener((o, oldVal, newVal)-> setContents(newVal));
        setContents(filterChoices.getSelectedToggle());

        itemsList = AbilityList.getTypeLevelList(items, (a, i) -> {
            display.getEngine().loadContent(AbilityHTMLGenerator.parse(a));
            if(i == 2) {
                if(character.meetsPrerequisites(a) && (a.isMultiple() || !character.abilities().haveAbility(a))) {
                    slot.add(a);
                }
            }
        });
        allItemsList = AbilityList.getTypeLevelList(allItems, (a, i) -> {
            display.getEngine().loadContent(AbilityHTMLGenerator.parse(a));
            if(i == 2) {
                if(character.meetsPrerequisites(a) && (a.isMultiple() || !character.abilities().haveAbility(a))) {
                    slot.add(a);
                }
            }
        });

        this.setCenter(itemsList);
    }

    private void setContents(Toggle newVal) {
        if(newVal instanceof RadioMenuItem && ((RadioMenuItem) newVal).getText().equals("All")) {
            setCenter(allItemsList);
        } else setCenter(itemsList);
    }

    @Override
    void setupChoicesListener() {
    }
}
