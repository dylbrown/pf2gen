package ui.ftl.wrap;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.attributes.Attribute;
import model.player.AttributeManager;
import model.player.PC;
import ui.ftl.entries.AttributeEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class AttributesWrapper implements TemplateHashModel {
    private final AttributeManager attributes;
    private final PF2GenObjectWrapper wrapper;
    private final PC character;

    public AttributesWrapper(AttributeManager attributes, PF2GenObjectWrapper wrapper, PC character) {
        this.character = character;
        this.attributes = attributes;
        this.wrapper = wrapper;
    }



    @SuppressWarnings("rawtypes")
    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        if(s.equals("get"))
            return wrapper.wrap((TemplateMethodModelEx) (List arguments) -> {
                Attribute attr = Attribute.valueOf(arguments.get(0).toString(), arguments.get(1).toString());
                return new AttributeEntry(character, attr,
                        attributes.getProficiency(attr),
                        character.levelProperty(),
                        wrapper);
            });
        for (Method method : attributes.getClass().getMethods()) {
            if (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                    && method.getParameterCount() == 0) {
                try {
                    return wrapper.wrap(method.invoke(attributes));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if (method.getName().equalsIgnoreCase(s)){
                return wrapper.wrap(method);
            }
        }
        Attribute attribute = Attribute.valueOf(s);
        if(attribute != null)
            return new AttributeEntry(character, attribute,
                    attributes.getProficiency(attribute),
                    character.levelProperty(),
                    wrapper);
        throw new TemplateModelException("Could not find member "+s+" of AttributeManager");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
