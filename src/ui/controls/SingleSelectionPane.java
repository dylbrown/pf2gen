package ui.controls;

import model.abilities.abilitySlots.SingleChoice;
import model.abilities.abilitySlots.SingleChoiceList;

import static ui.Main.character;


class SingleSelectionPane<T> extends SelectionPane<T> {
    SingleChoice<T> slot;

    SingleSelectionPane(SingleChoiceList<T> slot) {
        init(slot);
        items.addAll(slot.getOptions());
    }

    SingleSelectionPane() {}

    void init(SingleChoice<T> slot) {
        super.init(slot);
        this.slot = slot;
        if(slot.getChoice() != null)
            selections.add(slot.getChoice());
        slot.getChoiceProperty().addListener((o, oldVal, newVal)->{
            selections.remove(oldVal);
            if(newVal != null)
                selections.add(newVal);
        });
    }

    @Override
    void setupChoicesListener() {
        choices.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                T selectedItem = choices.getSelectionModel().getSelectedItem();
                if(selectedItem != null) {
                    character.choose(slot, selectedItem);
                }
            }
        });
    }

    @Override
    void setupChosenListener() {
        chosen.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                character.choose(slot, null);
            }
        });
    }
}
