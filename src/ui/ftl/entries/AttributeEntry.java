package ui.ftl.entries;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import model.attributes.Attribute;
import model.enums.Proficiency;
import model.player.PC;

public class AttributeEntry implements TemplateHashModel {
    private final Attribute attr;
    private final ObservableValue<Proficiency> prof;
    private final ObjectWrapper wrapper;
    private final ReadOnlyObjectProperty<Integer> level;
    private final String data;
    private final PC character;

    public AttributeEntry(PC character, Attribute attr, String data, ObservableValue<Proficiency> prof, ReadOnlyObjectProperty<Integer> levelProperty, ObjectWrapper objectWrapper) {
        this.character = character;
        this.attr = attr;
        this.data = data;
        this.prof = prof;
        this.level = levelProperty;
        this.wrapper = objectWrapper;
    }

    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        switch(s.toLowerCase()) {
            case "total": return wrapper.wrap(character.getTotalMod(attr, data));
            case "attribute": return wrapper.wrap(attr);
            case "proficiency": return wrapper.wrap(prof.getValue());
            case "proficiencymod": return wrapper.wrap(prof.getValue().getMod(level.get()));
            case "ability": return wrapper.wrap(attr.getKeyAbility());
            case "itembonus": return wrapper.wrap(character.attributes().getBonus(attr)); // TODO: Support general bonuses
            case "level": return wrapper.wrap(level.get());
            case "name": return wrapper.wrap(toString());
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        if(data != null && data.length() > 0) return attr.toString() + " (" + data + ")";
        return attr.toString();
    }
}
