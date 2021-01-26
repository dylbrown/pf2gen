package ui.todo;

import javafx.beans.value.ObservableValue;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.util.TransformationProperty;

public class AbilityScoreChoiceTodoItem extends AbstractTodoItem{
    private final AbilityModChoice mod;
    private final ObservableValue<Boolean> property;

    public AbilityScoreChoiceTodoItem(AbilityModChoice mod, Priority priority, Runnable navigateTo) {
        super(mod.getType().fancyName() + " Ability Boost", priority, navigateTo);
        this.mod = mod;
        this.property = new TransformationProperty<>(mod.getTargetProperty(), t -> t != AbilityScore.Free);
        priority.append(mod.getType().ordinal());
    }

    @Override
    public boolean isFinished() {
        return mod.getTarget() != AbilityScore.Free;
    }

    @Override
    public ObservableValue<Boolean> finishedProperty() {
        return property;
    }
}
