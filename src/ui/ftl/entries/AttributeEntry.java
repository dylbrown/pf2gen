package ui.ftl.entries;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import model.enums.Attribute;
import model.enums.Proficiency;

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
            case "total": return wrapper.wrap(prof.getValue().getMod(level.get()));
            case "attribute": return wrapper.wrap(attr);
            case "proficiency": return wrapper.wrap(prof.getValue());
            case "ability": return wrapper.wrap(attr.getKeyAbility());
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
