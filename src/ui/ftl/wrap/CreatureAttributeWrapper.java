package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.attributes.Attribute;

import java.util.Map;

public class CreatureAttributeWrapper implements TemplateHashModel {

    private final Map<Attribute, Integer> modifiers;
    private final ObjectWrapper wrapper;

    public CreatureAttributeWrapper(ObjectWrapper wrapper, Map<Attribute, Integer> modifiers) {
        this.wrapper = wrapper;
        this.modifiers = modifiers;
    }
    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        return wrapper.wrap(modifiers.get(Attribute.robustValueOf(s)));
    }

    @Override
    public boolean isEmpty() {
        return modifiers.isEmpty();
    }
}
