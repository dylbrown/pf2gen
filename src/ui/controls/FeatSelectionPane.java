package ui.controls;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.web.WebView;
import model.abilities.Ability;
import model.abilities.abilitySlots.AbilityChoiceList;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.SingleChoice;
import model.enums.Type;
import model.player.AbilityManager;
import ui.html.AbilityHTMLGenerator;

import java.util.ArrayList;
import java.util.List;

import static ui.Main.character;

public class FeatSelectionPane extends SingleSelectionPane<Ability> {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Ability> unmetPrereqs = new ArrayList<>();
    private final ObservableList<Ability> allItems = FXCollections.observableArrayList();

    public FeatSelectionPane(SingleChoice<Ability> slot, WebView display, ToggleGroup filterChoices) {
        this.display = display;
        AbilityManager abilities = character.abilities();
        init(slot);
        if(slot instanceof AbilityChoiceList) {
            allItems.addAll(((AbilityChoiceList) slot).getOptions());
            items.addAll(((AbilityChoiceList) slot).getOptions());
        }else{
            allItems.addAll(abilities.getOptions(slot));
            items.addAll(abilities.getOptions(slot));
        }
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
                        items.remove(ability);
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
        getSelectionModel().selectedItemProperty().addListener((event)->{
            Ability selectedItem = getSelectionModel().getSelectedItem();
            if(selectedItem != null)
                display.getEngine().loadContent(AbilityHTMLGenerator.generate(selectedItem));
        });
        if(slot instanceof FeatSlot) {
            if(((FeatSlot) slot).getAllowedTypes().contains(Type.Ancestry.toString())
                    || ((FeatSlot) slot).getAllowedTypes().contains(Type.Heritage.toString())){
                character.addAncestryObserver((observable) -> items.setAll(abilities.getOptions(slot)));
            }
        }
        filterChoices.selectedToggleProperty().addListener((o, oldVal, newVal)-> setContents(newVal));
        setContents(filterChoices.getSelectedToggle());
    }

    private void setContents(Toggle newVal) {
        if(newVal instanceof RadioMenuItem && ((RadioMenuItem) newVal).getText().equals("All")) {
            setItems(allItems);
        } else setItems(sortedItems);
    }

    @Override
    void setupChoicesListener() {
        setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                Ability selectedItem = getSelectionModel().getSelectedItem();
                if(selectedItem != null && character.meetsPrerequisites(selectedItem) &&
                        (selectedItem.isMultiple() || !character.abilities().haveAbility(selectedItem))) {
                    slot.fill(selectedItem);
                }
            }
        });
    }
}
