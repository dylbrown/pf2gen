package model.abilityScores;

import model.enums.AbilityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AbilityModChoice extends AbilityMod {
    private List<AbilityScore> choices;
    public AbilityModChoice(List<AbilityScore> choices, AbilityType source) {
        super(AbilityScore.Free, true, source);
        this.choices = choices;
    }

    public AbilityModChoice(AbilityType source) {
        super(AbilityScore.Free, true, source);
        this.choices = Arrays.asList(AbilityScore.scores());
    }

    public List<AbilityScore> getChoices() {
        return Collections.unmodifiableList(choices);
    }
}
