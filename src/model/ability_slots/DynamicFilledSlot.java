package model.ability_slots;

import model.abc.PClass;
import model.abilities.Ability;
import model.enums.Type;

import java.util.function.BiFunction;

public class DynamicFilledSlot extends AbilitySlot {
    private final Type type;
    private final String contents;
    private boolean hasClass = false;
    private final BiFunction<String, String, Ability> abilityFunction;
    private PClass pClass;

    public DynamicFilledSlot(String name, int level, String contents, Type type, BiFunction<String, String, Ability> abilityFunction) {
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
                        gettingAbility = true;
                        Ability classFeature = pClass.findClassFeature(contents);
                        if(classFeature != null) {
                            gettingAbility = false;
                            return classFeature;
                        }
                    }
                    return abilityFunction.apply(type.name(), contents);
                case Class:
                    if(hasClass){
                        Ability classFeat = pClass.findClassFeat(contents);
                        if(classFeat != null)
                            return classFeat;
                        return abilityFunction.apply(pClass.getName(), contents);
                    }
                default:
                    return null;
            }
    }

    @Override
    public AbilitySlot copy() {
        return this;
    }
}
