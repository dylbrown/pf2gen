package ui.ftl.wrap;

import freemarker.template.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class GenericWrapper<T> implements TemplateHashModel, TemplateScalarModel {
    private final T t;
    protected final ObjectWrapper wrapper;

    public GenericWrapper(T t, ObjectWrapper wrapper) {
        this.t = t;
        this.wrapper = wrapper;
    }

    abstract Object getSpecialCase(String s, T t);

    @Override
    public String getAsString() {
        return t.toString();
    }

    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        Object specialCase = getSpecialCase(s.toLowerCase(), t);
        if(specialCase == null) {
            for (Method method : t.getClass().getMethods()) {
                if (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                        && method.getParameterCount() == 0) {
                    try {
                        return wrapper.wrap(method.invoke(t));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        assert(false);
                    }
                }
            }
            throw new TemplateModelException("Could not find member "+s+" of "+this.getClass().getSimpleName());
        }
        return wrapper.wrap(specialCase);
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
