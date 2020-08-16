package ui.controllers.gm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.ability_slots.Choice;
import model.creatures.CreatureFamily;
import model.creatures.CustomCreature;
import model.creatures.Scale;
import model.data_managers.sources.SourcesLoader;
import model.enums.Trait;
import model.player.ArbitraryChoice;
import ui.controllers.DecisionsController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CreatureTabController extends DecisionsController {
    private final ObservableList<Choice<?>> choices = FXCollections.observableArrayList();
    private static final List<String> alignments = Arrays.asList("LG", "NG", "CG", "LN", "N", "CN", "LE", "NE", "CE");
    public TextField creatureName;
    public Spinner<Integer> creatureLevel;
    @FXML
    private ComboBox<Scale> strength, dexterity, constitution, intelligence, wisdom, charisma,
            fortitude, reflex, will, perception;

    @FXML
    private void initialize(){
        CustomCreature creature = new CustomCreature();
        add("Alignment",
                Trait.class,
                alignments.stream()
                        .map(s->SourcesLoader.instance().traits().find(s))
                        .collect(Collectors.toList()),
                creature.traits::add,
                creature.traits::remove
        );
        add("Size",
                Trait.class,
                SourcesLoader.instance().traits().getCategory("Size").values(),
                creature.traits::add,
                creature.traits::remove
        );
        add("Creature Type",
                Trait.class,
                SourcesLoader.instance().traits().getCategory("Creature Type").values(),
                creature.traits::add,
                creature.traits::remove
        );
        add("(Optional) Creature Family",
                CreatureFamily.class,
                SourcesLoader.instance().creatureFamilies().getAll().values(),
                creature::set,
                creature::unset
        );
        initialize(choices);
        List<ComboBox<Scale>> scales = Arrays.asList(strength, dexterity, constitution, intelligence, wisdom,
                charisma, fortitude, reflex, will, perception);
        for (ComboBox<Scale> scale : scales) {
            scale.getItems().addAll(Scale.values());
        }
        creatureLevel.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-2, 30, 1));
    }

    private <T> void add(String name, Class<T> tClass, Collection<T> values, Consumer<T> add, Consumer<T> remove) {
        List<T> list;
        if(values instanceof List)
            list = (List<T>) values;
        else {
            list = new ArrayList<>(values);
        }
        choices.add(
                new ArbitraryChoice<>(
                        name, list,
                        add, remove,
                        1, false, tClass
                ));
    }
}
