package model.ability_scores;

import model.enums.Type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AbilityModChoice extends AbilityMod implements Serializable {
    private static int counter = 0;
    private final int id;
    private final List<AbilityScore> choices;
    public AbilityModChoice(List<AbilityScore> choices, Type type) {
        super(AbilityScore.Free, true, type);
        this.choices = choices;
        this.id=counter++;
    }

    public AbilityModChoice(Type type) {
        super(AbilityScore.Free, true, type);
        this.choices = Arrays.asList(AbilityScore.scores());
        this.id=counter++;
    }

    public List<AbilityScore> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    public boolean pick(AbilityScore target) {
        if(choices.contains(target)){
            this.target.set(target);
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

    public boolean matches(AbilityModChoice other) {
        return this.choices.equals(other.choices) && this.getType().equals(other.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AbilityModChoice{" +
                "id=" + id +
                ", choices=" + choices +
                ", target=" + target +
                '}';
    }
}
