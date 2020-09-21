package model.ability_slots;

import model.abc.PClass;
import model.abilities.Ability;
import model.enums.Type;
import model.util.ObjectNotFoundException;

import java.util.function.BiFunction;

public class DynamicFilledSlot extends AbilitySlot {
    private final Type type;
    private final String contents;
    private boolean hasClass = false;
    private final BiFunction<Type, String, Ability> abilityFunction;
    private PClass pClass;

    public DynamicFilledSlot(String name, int level, String contents, Type type, BiFunction<Type, String, Ability> abilityFunction) {
        super(name, level);
        preSet = true;
        this.contents = contents;
        this.type = type;
        this.abilityFunction = abilityFunction;
    }

    public void setPClass(PClass pClass) {
        this.pClass = pClass;
        if(pClass != null)
            hasClass = true;
    }

   private boolean gettingAbility = false;

    @Override
    public synchronized Ability getCurrentAbility() {
            switch(type){
                case General:
                case Skill:
                case ClassFeature:
                    if(hasClass && !gettingAbility) {
                        try {
                            gettingAbility = true;
                            Ability classFeature = pClass.findClassFeature(contents);
                            gettingAbility = false;
                            return classFeature;
                        } catch (ObjectNotFoundException ignored) {
                            gettingAbility = false;
                        }
                    }
                    return abilityFunction.apply(type, contents);
                case Class:
                    if(hasClass){
                        return pClass.findFeat(contents);
                    }
                default:
                    return null;
            }
    }
}
