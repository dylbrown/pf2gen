package model.ability_slots;

import model.abc.PClass;
import model.abilities.Ability;
import model.data_managers.sources.SourcesLoader;
import model.enums.Type;

public class DynamicFilledSlot extends AbilitySlot {
    private final Type type;
    private final String contents;
    private final boolean hasClass;
    private PClass pClass;

    public DynamicFilledSlot(String name, int level, String contents, Type type, boolean hasClass) {
        super(name, level);
        preSet = true;
        this.contents = contents;
        this.type = type;
        this.hasClass = hasClass;
    }

    public void setpClass(PClass pClass) {
        this.pClass = pClass;
    }

    @Override
    public Ability getCurrentAbility() {
        switch(type){
            case General:
            case Skill:
                return SourcesLoader.instance().feats().find(contents);
            case Class:
                if(hasClass){
                    return pClass.findFeat(contents);
                }
            default:
                return null;
        }
    }
}
