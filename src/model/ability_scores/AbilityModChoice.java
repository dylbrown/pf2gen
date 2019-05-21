package model.ability_scores;

import model.enums.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AbilityModChoice extends AbilityMod {
    private static int counter = 0;
    private final int id;
    private final List<AbilityScore> choices;
    public AbilityModChoice(List<AbilityScore> choices, Type source) {
        super(AbilityScore.Free, true, source);
        this.choices = choices;
        this.id=counter++;
    }

    public AbilityModChoice(Type source) {
        super(AbilityScore.Free, true, source);
        this.choices = Arrays.asList(AbilityScore.scores());
        this.id=counter++;
    }

    public List<AbilityScore> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    public boolean pick(AbilityScore target) {
        if(choices.contains(target)){
            this.target = target;
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbilityModChoice that = (AbilityModChoice) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
