package ui.ftl.entries;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import model.attributes.Attribute;
import model.enums.Proficiency;
import ui.Main;

public class AttributeEntry implements TemplateHashModel {
    private final Attribute attr;
    private final ObservableValue<Proficiency> prof;
    private final ObjectWrapper wrapper;
    private final ReadOnlyObjectProperty<Integer> level;

    public AttributeEntry(Attribute attr, ObservableValue<Proficiency> prof, ReadOnlyObjectProperty<Integer> levelProperty, ObjectWrapper objectWrapper) {
        this.attr= attr;
        this.prof = prof;
        this.level = levelProperty;
        this.wrapper = objectWrapper;
    }

    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        switch(s.toLowerCase()) {
            case "total": return wrapper.wrap(Main.character.getTotalMod(attr));//TODO: Remove call to main
            case "attribute": return wrapper.wrap(attr);
            case "proficiency": return wrapper.wrap(prof.getValue());
            case "proficiencymod": return wrapper.wrap(prof.getValue().getMod(level.get()));
            case "ability": return wrapper.wrap(attr.getKeyAbility());
            case "itembonus": return wrapper.wrap(Main.character.attributes().getBonus(attr)); // TODO: Support general bonuses
            case "level": return wrapper.wrap(level.get());
            case "name": return wrapper.wrap(attr.name());
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return attr.toString();
    }
}
