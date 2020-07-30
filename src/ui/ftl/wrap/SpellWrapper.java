package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import model.spells.Spell;
import ui.html.SpellHTMLGenerator;

public class SpellWrapper extends MapGenericWrapper<Spell> {

    public SpellWrapper(Spell spell, ObjectWrapper wrapper) {
        super(spell, wrapper);
        map.put("desc", s -> SpellHTMLGenerator.getDescription(s, s.getLevelOrCantrip()));
        map.put("description", s -> SpellHTMLGenerator.getDescription(s, s.getLevelOrCantrip()));
    }

    public SpellWrapper(Spell spell, ObjectWrapper wrapper, int level) {
        super(spell, wrapper);
        map.put("desc", s -> SpellHTMLGenerator.getDescription(s, level));
        map.put("description", s -> SpellHTMLGenerator.getDescription(s, level));
    }
}
