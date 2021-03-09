package ui.controllers.gm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import model.CharacterManager;
import model.ability_slots.Choice;
import model.creatures.CreatureFamily;
import model.creatures.CustomCreatureCreator;
import model.creatures.Scale;
import model.enums.Trait;
import model.player.ArbitraryChoice;
import model.player.SourcesManager;
import model.util.ObjectNotFoundException;
import ui.controllers.DecisionsController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CreatureTabController {
    private final ObservableList<Choice<?>> choices = FXCollections.observableArrayList();
    private static final List<String> alignments = Arrays.asList("LG", "NG", "CG", "LN", "N", "CN", "LE", "NE", "CE");

    @FXML
    private TextField creatureName;
    @FXML
    private Spinner<Integer> creatureLevel;
    @FXML
    private Tab decisionPane;
    @FXML
    private ComboBox<Scale> strength, dexterity, constitution, intelligence, wisdom, charisma,
            fortitude, reflex, will, perception;

    @FXML
    private void initialize(){
        SourcesManager sources = CharacterManager.getActive().sources();
        CustomCreatureCreator creature = new CustomCreatureCreator(sources);
        add("Alignment",
                Trait.class,
                alignments.stream()
                        .map(s-> {
                            try {
                                return sources.traits().find(s);
                            } catch (ObjectNotFoundException e) {
                                e.printStackTrace();
                            }
                            return null;
                        })
                        .collect(Collectors.toList()),
                creature::addTrait,
                creature::removeTrait
        );
        add("Size",
                Trait.class,
                sources.traits().getCategory("Size").values(),
                creature::addTrait,
                creature::removeTrait
        );
        add("Creature Type",
                Trait.class,
                sources.traits().getCategory("Creature Type").values(),
                creature::addTrait,
                creature::removeTrait
        );
        add("(Optional) Creature Family",
                CreatureFamily.class,
                sources.creatureFamilies().getAll().values(),
                creature::set,
                creature::unset
        );
        List<ComboBox<Scale>> scales = Arrays.asList(strength, dexterity, constitution, intelligence, wisdom,
                charisma, fortitude, reflex, will, perception);
        for (ComboBox<Scale> scale : scales) {
            scale.getItems().addAll(Scale.values());
        }
        creatureLevel.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-2, 30, 1));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/decisionsTab.fxml"));
        DecisionsController controller = new DecisionsController(choices);
        loader.setController(controller);
        try {
            decisionPane.setContent(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T> void add(String name, Class<T> tClass, Collection<T> values, Consumer<T> add, Consumer<T> remove) {
        List<T> list;
        if(values instanceof List)
            list = (List<T>) values;
        else {
            list = new ArrayList<>(values);
        }
        ArbitraryChoice.Builder<T> choice = new ArbitraryChoice.Builder<>();
        choice.setName(name);
        choice.setChoicesConstant(list);
        choice.setFillFunction(add);
        choice.setEmptyFunction(remove);
        choice.setMaxSelections(1);
        choice.setMultipleSelect(false);
        choice.setOptionsClass(tClass);
        choices.add(choice.build());
    }
}
