package model.abilities;

import model.enums.Proficiency;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class RequirementList<T> implements Requirement<T> {
    private final List<Requirement<T>> requirements;
    private final boolean all;
    public RequirementList(List<Requirement<T>> requirements, boolean all) {
        this.requirements = Collections.unmodifiableList(requirements);
        this.all = all;
    }

    public List<Requirement<T>> getRequirements() {
        return requirements;
    }

    public boolean isAll() {
        return all;
    }

    @Override
    public boolean test(Function<T, Proficiency> getProficiency) {
        if(all) {
            for (Requirement<T> requirement : requirements) {
                if(!requirement.test(getProficiency))
                    return false;
            }
            return true;
        } else {
            for (Requirement<T> requirement : requirements) {
                if(requirement.test(getProficiency))
                    return true;
            }
            return false;
        }
    }
}
