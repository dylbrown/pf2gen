package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MapGenericWrapper<T> extends GenericWrapper<T> {
    protected final Map<String, Function<T, Object>> map = new HashMap<>();
    public MapGenericWrapper(T t, ObjectWrapper wrapper) {
        super(t, wrapper);
    }

    @Override
    boolean hasSpecialCase(String s) {
        return map.containsKey(s);
    }

    @Override
    Object getSpecialCase(String s, T t) {
        return map.get(s).apply(t);
    }
}
