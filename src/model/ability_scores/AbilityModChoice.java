package model.ability_scores;

import model.enums.Type;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static model.util.Copy.copy;

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
        this.choices = AbilityScore.scores();
        this.id=counter++;
    }

    @SuppressWarnings("IncompleteCopyConstructor")
    public AbilityModChoice(AbilityModChoice other) {
        super(AbilityScore.Free, true, other.getType());
        this.id = counter++;
        this.choices = copy(other.choices);
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

    public boolean matches(Type type, List<AbilityScore> choices) {
        return this.choices.equals(choices) && this.getType().equals(type);
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
                ", type=" + getType() +
                '}';
    }

    public void reset() {
        this.target.set(AbilityScore.Free);
    }
}
