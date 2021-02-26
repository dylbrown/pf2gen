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
import model.util.Pair;
import model.util.TransformationProperty;
import ui.controls.FeatSelectionPane;
import ui.controls.SelectionPane;
import ui.controls.lists.DecisionsList;
import ui.controls.lists.entries.ListEntry;
import ui.html.HTMLGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DecisionsController {

    @FXML
    private TextField search, decisionsSearch;
    @FXML
    private MenuBar optionsMenu, decisionsOptionsMenu;
    @FXML
    private RadioMenuItem firstCategory, firstLevel, secondNone, secondCategory, secondLevel, filterQualified;
    @FXML
    private RadioMenuItem decisionsFirstCategory, decisionsFirstLevel, decisionsSecondNone, decisionsSecondCategory, decisionsSecondLevel, decisionsFilterUndecided;
    @FXML
    private ToggleGroup groupBy, groupBySecond, filterChoices;
    @FXML
    private ToggleGroup decisionsGroupBy, decisionsGroupBySecond, decisionsFilterChoices;
    @FXML
    private WebView display;
    @FXML
    private BorderPane decisionsPaneContainer, choicesContainer;
    private final Map<Choice<?>, Node> nodes = new HashMap<>();
    private DecisionsList decisionsList;
    private final ReadOnlyObjectWrapper<Pair<Function<Ability, String>, String>> categoryFunctionProperty =
            new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Pair<Function<Ability, String>, String>> subCategoryFunctionProperty =
            new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Pair<Function<Choice<?>, String>, String>> decisionsCategoryFunctionProperty =
            new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Pair<Function<Choice<?>, String>, String>> decisionsSubCategoryFunctionProperty =
            new ReadOnlyObjectWrapper<>();

    @FXML
    private void initialize() {
        initialize(CharacterManager.getActive().decisions().getDecisions());
    }

    protected void initialize(ObservableList<Choice<?>> decisions) {
        display.getEngine().setUserStyleSheetLocation(getClass().getResource("/webview_style.css").toString());
        groupBy.selectedToggleProperty().addListener((observableValue, oldVal, newVal) -> updateGroupBy(newVal, categoryFunctionProperty));
        groupBySecond.selectedToggleProperty().addListener((observableValue, oldVal, newVal) -> updateGroupBy(newVal, subCategoryFunctionProperty));
        updateGroupBy(groupBy.getSelectedToggle(), categoryFunctionProperty);
        updateGroupBy(groupBySecond.getSelectedToggle(), subCategoryFunctionProperty);
        decisionsGroupBy.selectedToggleProperty().addListener((observableValue, oldVal, newVal) -> updateDecisionGroupBy(newVal, decisionsCategoryFunctionProperty));
        decisionsGroupBySecond.selectedToggleProperty().addListener((observableValue, oldVal, newVal) -> updateDecisionGroupBy(newVal, decisionsSubCategoryFunctionProperty));
        updateDecisionGroupBy(decisionsGroupBy.getSelectedToggle(), decisionsCategoryFunctionProperty);
        updateDecisionGroupBy(decisionsGroupBySecond.getSelectedToggle(), decisionsSubCategoryFunctionProperty);
        DecisionsList.Builder builder = new DecisionsList.Builder();
        builder.setDisplay(display);
        builder.setOptions(decisions);
        builder.setSelectHandler(this::setChoices);
        builder.setSearch(decisionsSearch.textProperty());
        builder.setCategoryFunctionProperty(decisionsCategoryFunctionProperty.getReadOnlyProperty());
        builder.setSubCategoryFunctionProperty(decisionsSubCategoryFunctionProperty.getReadOnlyProperty());
        builder.setFilterByRemaining(new TransformationProperty<>(decisionsFilterChoices.selectedToggleProperty(), t->t == decisionsFilterUndecided));
        decisionsList = builder.build();
        decisionsPaneContainer.setCenter(decisionsList);
    }

    private void updateGroupBy(Toggle toggle, ReadOnlyObjectWrapper<Pair<Function<Ability, String>, String>> functionProperty) {
        if(toggle == firstCategory || toggle == secondCategory)
            functionProperty.set(new Pair<>(a->{
                ArchetypeExtension archetype = a.getExtension(ArchetypeExtension.class);
                if(archetype != null && !archetype.isDedication()) {
                    return "Archetype";
                }
                return a.getType().name();
            }, "Category"));
        else if(toggle == firstLevel || toggle == secondLevel)
            functionProperty.set(new Pair<>(a->"Level " + a.getLevel(), "Level"));
        else functionProperty.set(new Pair<>(a->"", ""));
    }

    private void updateDecisionGroupBy(Toggle toggle, ReadOnlyObjectWrapper<Pair<Function<Choice<?>, String>, String>> functionProperty) {
        if(toggle == decisionsFirstCategory || toggle == decisionsSecondCategory)
            functionProperty.set(new Pair<>(c->{
                if(c instanceof FeatSlot)
                    return ((FeatSlot) c).getAllowedTypes().stream()
                            .map(s->(s.equalsIgnoreCase("feat")) ? s + "s" : s)
                            .collect(Collectors.joining(" "));
                return "Choices";
            }, "Category"));
        else if(toggle == decisionsFirstLevel || toggle == decisionsSecondLevel)
            functionProperty.set(new Pair<>(a->"Level " + a.getLevel(), "Level"));
        else functionProperty.set(new Pair<>(a->"", ""));
    }

    private void setChoices(Choice<?> choice) {
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
    }

    private <T> SelectionPane<T, ?> getPane(Choice<T> choice) {
        SelectionPane.Builder<T, ListEntry<T>> builder = SelectionPane.Builder.makeDefault();
        builder.setChoice(choice);
        return getPane(builder);
    }

    private <T> SelectionPane<T, ?> getPane(SelectionPane.Builder<T, ?> builder) {
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
        decisionsList.navigate(path);
    }
}
