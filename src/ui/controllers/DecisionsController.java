package ui.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import model.CharacterManager;
import model.abilities.Ability;
import model.abilities.ArchetypeExtension;
import model.ability_slots.Choice;
import model.ability_slots.ChoiceList;
import model.ability_slots.FeatSlot;
import model.ability_slots.SingleChoiceSlot;
import model.util.TransformationProperty;
import ui.controls.FeatSelectionPane;
import ui.controls.SelectionPane;
import ui.controls.lists.DecisionsList;
import ui.controls.lists.entries.DecisionEntry;
import ui.html.HTMLGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class DecisionsController {

    @FXML
    private TextField search;
    @FXML
    private MenuBar optionsMenu;
    @FXML
    private RadioMenuItem firstCategory, firstLevel, secondNone, secondCategory, secondLevel, filterQualified;
    @FXML
    private ToggleGroup groupBy, groupBySecond, filterChoices;
    @FXML
    private WebView display;
    @FXML
    private BorderPane decisionsPaneContainer, choicesContainer;
    private final Map<Choice, Node> nodes = new HashMap<>();
    private DecisionsList decisionsList;
    private final ReadOnlyObjectWrapper<Function<Ability, String>> categoryFunctionProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Function<Ability, String>> subCategoryFunctionProperty = new ReadOnlyObjectWrapper<>();

    @FXML
    private void initialize() {
        initialize(CharacterManager.getActive().decisions().getDecisions());
    }

    protected void initialize(ObservableList<Choice> decisions) {
        display.getEngine().setUserStyleSheetLocation(getClass().getResource("/webview_style.css").toString());
        groupBy.selectedToggleProperty().addListener((observableValue, oldVal, newVal) -> updateGroupBy(newVal, categoryFunctionProperty));
        groupBySecond.selectedToggleProperty().addListener((observableValue, oldVal, newVal) -> updateGroupBy(newVal, subCategoryFunctionProperty));
        updateGroupBy(groupBy.getSelectedToggle(), categoryFunctionProperty);
        updateGroupBy(groupBySecond.getSelectedToggle(), subCategoryFunctionProperty);
        decisionsList = new DecisionsList((treeItem, i) -> setChoices(treeItem),
                decisions);
        decisionsList.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal)->setChoices(newVal));
        decisionsPaneContainer.setCenter(decisionsList);
    }

    private void updateGroupBy(Toggle toggle, ReadOnlyObjectWrapper<Function<Ability, String>> functionProperty) {
        if(toggle == firstCategory || toggle == secondCategory)
            functionProperty.set(a->{
                ArchetypeExtension archetype = a.getExtension(ArchetypeExtension.class);
                if(archetype != null && !archetype.isDedication()) {
                    return "Archetype";
                }
                return a.getType().name();
            });
        else if(toggle == firstLevel || toggle == secondLevel)
            functionProperty.set(a->"Level " + a.getLevel());
        else functionProperty.set(a->"");
    }

    private Choice<?> setChoices(TreeItem<DecisionEntry> treeItem) {
        if(treeItem == null) return null;
        DecisionEntry entry = treeItem.getValue();
        Choice<?> choice = entry.getChoice();
        if(choice == null) {
            choice = setChoices(treeItem.getParent());
            setDisplay(choice, entry.getChosenValue());
            return choice;
        }
        Node node = nodes.get(choice);
        if(node == null) {
            if(choice.getOptionsClass() == Ability.class) {
                FeatSelectionPane.Builder featBuilder = new FeatSelectionPane.Builder();
                if (choice instanceof FeatSlot) {
                    featBuilder.setChoice((FeatSlot) choice);
                } else if (choice instanceof SingleChoiceSlot) {
                    featBuilder.setChoice((SingleChoiceSlot) choice);
                }
                featBuilder.setCategoryFunctionProperty(categoryFunctionProperty.getReadOnlyProperty());
                featBuilder.setSubCategoryFunctionProperty(subCategoryFunctionProperty.getReadOnlyProperty());
                featBuilder.setFilterByPrerequisites(new TransformationProperty<>(filterChoices.selectedToggleProperty(), t->t == filterQualified));
                node = getPane(featBuilder);
            }else if(choice instanceof ChoiceList){
                node = getPane((ChoiceList<?>) choice);
            }else {
                node = new AnchorPane();
            }
            nodes.put(choice, node);
        }
        choicesContainer.setCenter(node);

        return choice;
    }

    private <T> SelectionPane<T> getPane(Choice<T> choice) {
        SelectionPane.Builder<T> builder = new SelectionPane.Builder<>();
        builder.setChoice(choice);
        return getPane(builder);
    }

    private <T> SelectionPane<T> getPane(SelectionPane.Builder<T> builder) {
        builder.setDisplay(display);
        builder.setSearch(search.textProperty());
        return builder.build();
    }

    private <T> void setDisplay(Choice<T> choice, Object chosenValue) {
        Function<T, String> generator = HTMLGenerator.getGenerator(choice.getOptionsClass());
        if(chosenValue != null) {
            display.getEngine().loadContent(
                    generator.apply(choice.getOptionsClass().cast(chosenValue))
            );
        }
    }

    public void navigate(List<String> path) {
        for (TreeItem<DecisionEntry> choice : decisionsList.getRoot().getChildren()) {
            if(path.get(0).equalsIgnoreCase(choice.getValue().toString())) {
                decisionsList.getSelectionModel().select(choice);
                break;
            }
        }
    }
}
