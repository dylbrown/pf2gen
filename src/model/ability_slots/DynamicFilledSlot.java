package model.ability_slots;

import model.abc.PClass;
import model.abilities.Ability;
import model.data_managers.sources.SourcesLoader;
import model.enums.Type;
import model.util.ObjectNotFoundException;

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
                try {
                    return SourcesLoader.ALL_SOURCES.feats().find(contents);
                } catch (ObjectNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            case Class:
                if(hasClass){
                    return pClass.findFeat(contents);
                }
            default:
                return null;
        }
    }
}
