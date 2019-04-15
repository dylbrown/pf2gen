package model.abilityScores;

import model.enums.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AbilityModChoice extends AbilityMod {
    private List<AbilityScore> choices;
    public AbilityModChoice(List<AbilityScore> choices, Type source) {
        super(AbilityScore.Free, true, source);
        this.choices = choices;
    }

    public AbilityModChoice(Type source) {
        super(AbilityScore.Free, true, source);
        this.choices = Arrays.asList(AbilityScore.scores());
    }

    public List<AbilityScore> getChoices() {
        return Collections.unmodifiableList(choices);
    }
}
