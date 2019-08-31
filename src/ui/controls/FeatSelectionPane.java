package ui.controls;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import model.abilities.Ability;
import model.abilities.abilitySlots.SingleChoice;
import model.abilities.abilitySlots.ChoiceList;
import model.abilities.abilitySlots.FeatSlot;
import model.enums.Type;
import model.player.AbilityManager;

import java.util.ArrayList;
import java.util.List;

import static ui.Main.character;

public class FeatSelectionPane extends SingleSelectionPane<Ability> {
    private List<Ability> unmetPrereqs = new ArrayList<>();

    public FeatSelectionPane(SingleChoice<Ability> slot) {
        AbilityManager abilities = character.abilities();
        init(slot);
        if(slot instanceof ChoiceList)
            items.addAll(((ChoiceList<Ability>) slot).getOptions());
        else
            items.addAll(abilities.getOptions(slot));
        Label desc = new Label();
        desc.setWrapText(true);
        AnchorPane.setLeftAnchor(desc, 0.0);
        AnchorPane.setRightAnchor(desc, 0.0);
        AnchorPane.setTopAnchor(desc, 0.0);
        AnchorPane.setBottomAnchor(desc, 0.0);
        side.getItems().add(new AnchorPane(desc));
        side.setDividerPositions(.25);
        items.removeIf((item)->{
            if(!character.meetsPrerequisites(item)){
                unmetPrereqs.add(item);
                return true;
            }
            return false;
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
                    //unmetPrereqs.addAll(event.getAddedSubList());
                }
                if (event.wasRemoved()) {
                    unmetPrereqs.removeAll(event.getRemoved());
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
        choices.getSelectionModel().selectedItemProperty().addListener((event)->{
            Ability selectedItem = choices.getSelectionModel().getSelectedItem();
            if(selectedItem != null)
                desc.setText(selectedItem.getDesc());
        });
        if(slot instanceof FeatSlot) {
            if(((FeatSlot) slot).getAllowedTypes().contains(Type.Ancestry)) {
                character.addAncestryObserver((observable, arg) -> items.setAll(abilities.getOptions(slot)));
            }else if(((FeatSlot) slot).getAllowedTypes().contains(Type.Heritage)){
                character.addAncestryObserver((observable, arg) -> items.setAll(abilities.getOptions(slot)));
            }
        }
    }

    @Override
    void setupChoicesListener() {
        choices.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                Ability selectedItem = choices.getSelectionModel().getSelectedItem();
                if(selectedItem != null && character.meetsPrerequisites(selectedItem) &&
                        (selectedItem.isMultiple() || !character.abilities().haveAbility(selectedItem))) {
                    character.choose(slot, selectedItem);
                }
            }
        });
    }
}
