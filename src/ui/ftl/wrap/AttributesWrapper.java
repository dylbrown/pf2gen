package ui.ftl.wrap;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.attributes.Attribute;
import model.player.AttributeManager;
import ui.ftl.entries.AttributeEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static ui.Main.character;

public class AttributesWrapper implements TemplateHashModel {
    private final AttributeManager attributes;
    private final PF2GenObjectWrapper wrapper;

    public AttributesWrapper(AttributeManager attributes, PF2GenObjectWrapper wrapper) {
        this.attributes = attributes;
        this.wrapper = wrapper;
    }



    @SuppressWarnings("rawtypes")
    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        if(s.equals("get"))
            return wrapper.wrap((TemplateMethodModelEx) (List arguments) -> {
                Attribute attr = Attribute.robustValueOf(arguments.get(0).toString());
                return new AttributeEntry(attr,
                        arguments.get(1).toString(),
                        attributes.getProficiency(attr),
                        character.levelProperty(),
                        wrapper);
            });
        for (Method method : attributes.getClass().getMethods()) {
            if (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                    && method.getParameterCount() == 0) {
                try {
                    return wrapper.wrap(method.invoke(character));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if (method.getName().toLowerCase().equals(s.toLowerCase())){
                return wrapper.wrap(method);
            }
        }
        Attribute attribute = Attribute.robustValueOf(s);
        if(attribute != null)
            return new AttributeEntry(attribute,
                    "",
                    attributes.getProficiency(attribute),
                    character.levelProperty(),
                    wrapper);
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
