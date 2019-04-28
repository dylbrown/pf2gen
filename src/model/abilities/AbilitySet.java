package model.abilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbilitySet extends Ability {
    private List<Ability> abilities;
    public AbilitySet(String name, String desc, List<Ability> abilities) {
        super(name, desc);
        this.abilities = abilities;
        modifiers = new ArrayList<>();
        for(Ability ability: abilities) {
            modifiers.addAll(ability.getModifiers());
        }
    }

    public List<Ability> getAbilities(){
        return Collections.unmodifiableList(abilities);
    }
}
