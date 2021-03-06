package ui.controllers;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.CharacterManager;
import model.ability_scores.AbilityModChoice;
import model.ability_slots.Choice;
import model.enums.Alignment;
import model.player.PC;
import model.spells.SpellList;
import ui.Main;
import ui.todo.*;

import java.util.*;

public class InfoTabController {
	@FXML
	private ListView<TodoItem> todoList;
    @FXML
	private ComboBox<Alignment> alignment;
	@FXML
	private TextField characterName, playerName, height, weight, age, hair, eyes, gender;
	@FXML
	private Label level;
	@FXML
	private Button levelUp, levelDown;
	private PC character;
	private final ObservableList<TodoItem> todos = FXCollections.observableArrayList(todo->
			new Observable[]{todo.finishedProperty()});
	private final Map<Choice<?>, TodoItem> choiceTodos = new HashMap<>();
	private final Map<AbilityModChoice, TodoItem> scoreTodos = new HashMap<>();
	private final Map<SpellList, List<TodoItem>> spellListTodos = new HashMap<>();

	@SuppressWarnings("rawtypes")
	@FXML
	void initialize() {
		character = CharacterManager.getActive();
		alignment.getItems().addAll(Alignment.values());
		alignment.getSelectionModel().selectedItemProperty()
				.addListener((o, oldValue, newValue)-> character.setAlignment(newValue));
		alignment.getSelectionModel().select(character.getAlignment());
		character.alignmentProperty().addListener(c ->
				alignment.getSelectionModel().select(character.getAlignment()));
		characterName.textProperty().bindBidirectional(character.qualities().getProperty("name"));
		playerName.textProperty().bindBidirectional(character.qualities().getProperty("player"));
		height.textProperty().bindBidirectional(character.qualities().getProperty("height"));
		weight.textProperty().bindBidirectional(character.qualities().getProperty("weight"));
		age.textProperty().bindBidirectional(character.qualities().getProperty("age"));
		eyes.textProperty().bindBidirectional(character.qualities().getProperty("eyes"));
		hair.textProperty().bindBidirectional(character.qualities().getProperty("hair"));
		gender.textProperty().bindBidirectional(character.qualities().getProperty("gender"));
		level.setText(character.levelProperty().get().toString());
		character.levelProperty().addListener((event)-> level.setText(character.levelProperty().get().toString()));
		levelUp.setOnAction((event -> character.levelUp()));
		levelDown.setOnAction((event -> character.levelDown()));

		todoList.setOnMouseClicked(e->{
			if(e.getClickCount() == 2)
				todoList.getSelectionModel().getSelectedItem().navigateTo();
		});

		SortedList<TodoItem> sorted = new SortedList<>(todos, Comparator.comparing(TodoItem::getPriority));
		FilteredList<TodoItem> filtered = new FilteredList<>(sorted, todo->!todo.finishedProperty().getValue());
		todoList.setItems(filtered);
		todos.addAll(
				new PropertyTodoItem<>("Ancestry",
						character.ancestryProperty(),
						new Priority(1.0, 1.0),
						()->navigate("tab_startingChoices", "Ancestry")),
				new PropertyTodoItem<>("Background",
						character.backgroundProperty(),
						new Priority(1.0, 2.0),
						()->navigate("tab_startingChoices", "Background")),
				new PropertyTodoItem<>("Class",
						character.pClassProperty(),
						new Priority(1.0, 3.0),
						()->navigate("tab_startingChoices", "Class")),
				new PropertyTodoItem<>("Deity",
						character.deityProperty(),
						new Priority(1.0, 4.0),
						()->navigate("tab_startingChoices", "Deity")),
				new FormulasKnownTodoItem(character,
						new Priority(4.0),
						()->navigate("tab_equipment", "tab_formulas"))
		);

		//Ability Scores
		for (AbilityModChoice score : character.scores().getAbilityScoreChoices()) {
			addScoreChoice(score);
		}
		character.scores().getAbilityScoreChoices().addListener((ListChangeListener<AbilityModChoice>) change -> {
			while(change.next()) {
				if(change.wasAdded()) {
					for (AbilityModChoice choice : change.getAddedSubList()) {
						addScoreChoice(choice);
					}
				}
				if(change.wasRemoved()) {
					for (AbilityModChoice choice : change.getRemoved()) {
						removeScoreChoice(choice);
					}
				}
			}
		});

		//Decisions
		for (Choice decision : character.decisions().getDecisions()) {
			addDecision(decision);
		}
		character.decisions().getDecisions().addListener((ListChangeListener<Choice>) change -> {
			while(change.next()) {
				for (Choice choice : change.getAddedSubList()) {
					addDecision(choice);
				}
				for (Choice choice : change.getRemoved()) {
					removeDecision(choice);
				}
			}
		});


		//Spells
		for (Map.Entry<String, SpellList> entry : character.spells().getSpellLists().entrySet()) {
			addSpellList(entry.getValue());
		}
		character.spells().getSpellLists().addListener((MapChangeListener<String, SpellList>) change -> {
			if(change.wasAdded()) {
				addSpellList(change.getValueAdded());
			}
			if(change.wasRemoved()) {
				removeSpellList(change.getValueRemoved());
			}
		});
	}

	private void addScoreChoice(AbilityModChoice score) {
		AbilityScoreChoiceTodoItem todoItem = new AbilityScoreChoiceTodoItem(score, new Priority(1.5), () ->
				navigate("tab_abilityScores"));
		scoreTodos.put(score, todoItem);
		todos.add(todoItem);
	}

	private void removeScoreChoice(AbilityModChoice score) {
		TodoItem todoItem = scoreTodos.remove(score);
		todos.remove(todoItem);
	}

	private void addDecision(Choice<?> choice) {
		ChoiceTodoItem<?> todoItem = new ChoiceTodoItem<>(choice, new Priority(2.0), () ->
				navigate("tab_decisions", choice.getName() + " " + choice.getLevel()));
		choiceTodos.put(choice, todoItem);
		todos.add(todoItem);
	}

	private void removeDecision(Choice<?> choice) {
		TodoItem todoItem = choiceTodos.remove(choice);
		todos.remove(todoItem);
	}

	private void addSpellList(SpellList list) {
		List<TodoItem> spellsKnownTodos = new ArrayList<>();
		for(int i = 0; i <= 10; i++) {
			SpellsKnownTodoItem todo = new SpellsKnownTodoItem(list, i, new Priority(3.0), () ->
					navigate("tab_spells", list.getIndex()));
			spellsKnownTodos.add(todo);
			todos.add(todo);
		}
		spellListTodos.put(list, spellsKnownTodos);
	}

	private void removeSpellList(SpellList list) {
		List<TodoItem> todoItems = spellListTodos.remove(list);
		for (TodoItem todo : todoItems) {
			todos.remove(todo);
		}
	}

	private void navigate(String...path) {
		List<String> pathList = new ArrayList<>(Arrays.asList(path));
		Main.CONTROLLER.navigate(pathList);
	}
}
