package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import model.spells.SpellList;
import model.util.WrapperTransformationList;

public class SpellListWrapper extends GenericWrapper<SpellList> {
    public SpellListWrapper(SpellList spellList, ObjectWrapper wrapper) {
        super(spellList, wrapper);

    }

    @Override
    Object getSpecialCase(String s, SpellList spellList) {
        if(!s.equals("spellsknown")) return null;
        return new WrapperTransformationList<>(
                spellList.getSpellsKnown(),
                ol -> new WrapperTransformationList<>(
                        ol, spell ->
                        new SpellWrapper(spell, wrapper, spellList.getHighestLevelCanCast()
                        )
                )
        );
    }
}
