package model.ability_slots;

import model.abc.PClass;
import model.abilities.Ability;
import model.enums.Type;

import java.util.function.Function;

public class DynamicFilledSlot extends AbilitySlot {
    private final Type type;
    private final String contents;
    private final boolean hasClass;
    private final Function<String, Ability> abilityFunction;
    private PClass pClass;

    public DynamicFilledSlot(String name, int level, String contents, Type type, boolean hasClass, Function<String, Ability> abilityFunction) {
        super(name, level);
        preSet = true;
        this.contents = contents;
        this.type = type;
        this.hasClass = hasClass;
        this.abilityFunction = abilityFunction;
    }

    public void setPClass(PClass pClass) {
        this.pClass = pClass;
    }

    @Override
    public Ability getCurrentAbility() {
        switch(type){
            case General:
            case Skill:
                return abilityFunction.apply(contents);
            case Class:
                if(hasClass){
                    return pClass.findFeat(contents);
                }
            default:
                return null;
        }
    }
}
