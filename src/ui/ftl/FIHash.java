package ui.ftl;

import freemarker.template.*;

import java.util.function.Function;
import java.util.function.Supplier;

public class FIHash implements TemplateHashModel {
    private final ObjectWrapper wrapper;
    private final Function<String, Object> getter;
    private final Supplier<Boolean> empty;
    public FIHash(Function<String, Object> getter, Supplier<Boolean> empty, ObjectWrapper objectWrapper){
        this.getter = getter;
        this.empty = empty;
        this.wrapper = objectWrapper;
    }
    @Override
    public TemplateModel get(String s) throws TemplateModelException { return wrapper.wrap(getter.apply(s)); }
    @Override
    public boolean isEmpty() { return empty.get(); }
}
