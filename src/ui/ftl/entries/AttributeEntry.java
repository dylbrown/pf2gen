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
        switch(s) {
            case "total": return wrapper.wrap(Main.character.getTotalMod(attr));
            case "attribute": return wrapper.wrap(attr);
            case "proficiency": return wrapper.wrap(prof.getValue());
            case "ability": return wrapper.wrap(attr.getKeyAbility());
            case "level": return wrapper.wrap(level.get());
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
