package model.abilities.abilitySlots;

import model.abilities.Ability;
import model.enums.Type;
import model.xml_parsers.FeatsLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ui.Main.character;

public class FeatSlot extends AbilitySlot implements Pickable {
    private final List<Type> allowedTypes;
    private static FeatsLoader generalFeats = new FeatsLoader("data/feats/general.pfdyl");
    private static FeatsLoader skillFeats = new FeatsLoader("data/feats/skill.pfdyl");

    public FeatSlot(String name, int level, List<Type> allowedTypes) {
        super(name, level);
        this.allowedTypes = allowedTypes;
    }

    public List<Type> getAllowedTypes() {
        return Collections.unmodifiableList(allowedTypes);
    }

    @Override
    public List<Ability> getAbilities(int level) {
        List<Ability> results = new ArrayList<>();
        for (Type allowedType : allowedTypes) {
            switch(allowedType) {
                case Class:
                    if(character.getPClass() != null)
                        results.addAll(character.getPClass().getFeats(level));
                    break;
                case Ancestry:
                    if(character.getAncestry() != null)
                        results.addAll(character.getAncestry().getFeats(level));
                    break;
                case Heritage:
                    if(character.getAncestry() != null)
                        results.addAll(character.getAncestry().getHeritages());
                    break;
                case General:
                    results.addAll(generalFeats.parse());
                    break;
                case Skill:
                    results.addAll(skillFeats.parse());
                    break;
            }
        }
        return results;
    }

    @Override
    public void fill(Ability choice) {
        currentAbility = choice;
    }

}
