package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class GenericWrapper<T> implements TemplateHashModel {
    private final T t;
    protected final ObjectWrapper wrapper;

    public GenericWrapper(T t, ObjectWrapper wrapper) {
        this.t = t;
        this.wrapper = wrapper;
    }

    abstract boolean hasSpecialCase(String s);
    abstract Object getSpecialCase(String s, T t);

    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        if(!hasSpecialCase(s.toLowerCase())) {
            for (Method method : t.getClass().getMethods()) {
                if (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                        && method.getParameterCount() == 0) {
                    try {
                        return wrapper.wrap(method.invoke(t));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            throw new TemplateModelException("Could not find member "+s+" of "+this.getClass().getSimpleName());
        }
        return wrapper.wrap(getSpecialCase(s, t));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return t.toString();
    }
}
