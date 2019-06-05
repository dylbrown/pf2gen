package ui.controls;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import model.abilities.Ability;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.ChoiceList;
import model.abilities.abilitySlots.FeatSlot;
import model.enums.Type;
import model.player.AbilityManager;

import java.util.ArrayList;
import java.util.List;

import static ui.Main.character;

public class FeatSelectionPane extends SelectionPane<Ability> {
    private List<Ability> unmetAndTaken = new ArrayList<>();

    public FeatSelectionPane(Choice<Ability> slot) {
        AbilityManager abilities = character.abilities();
        init(slot);
        if(slot instanceof ChoiceList)
            items.addAll(((ChoiceList<Ability>) slot).getOptions());
        else
            items.addAll(abilities.getOptions(slot));
        Label desc = new Label();
        desc.setWrapText(true);
        side.setTop(desc);
        items.removeIf((item)->{
            if(!character.meetsPrerequisites(item) || abilities.getAbilities().contains(item)){
                unmetAndTaken.add(item);
                return true;
            }
            return false;
        });
        abilities.getAbilities().addListener((ListChangeListener<Ability>) (event)->{
            while(event.next()) {
                if (event.wasAdded()) {
                    items.removeAll(event.getAddedSubList());
                    unmetAndTaken.removeIf((item) -> {
                        if (character.meetsPrerequisites(item) && !abilities.getAbilities().contains(item)) {
                            items.add(item);
                            return true;
                        }
                        return false;
                    });
                    unmetAndTaken.addAll(event.getAddedSubList());
                }
                if (event.wasRemoved()) {
                    unmetAndTaken.removeAll(event.getRemoved());
                    for (Ability item : event.getRemoved()) {
                        if(abilities.getOptions(slot).contains(item)){
                            if (!character.meetsPrerequisites(item) || abilities.getAbilities().contains(item)) {
                                unmetAndTaken.add(item);
                            }else items.add(item);
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
}
