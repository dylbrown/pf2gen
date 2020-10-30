package model.util;

import freemarker.template.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DataTemplateMap implements Map<String, TemplateModel> {

    private final Map<String, TemplateModel> sourceMap;
    private final ObjectWrapper wrapper;

    public DataTemplateMap(Map<String, TemplateModel> sourceMap, ObjectWrapper objectWrapper) {
        this.sourceMap = sourceMap;
        this.wrapper = objectWrapper;
    }

    @Override
    public int size() {
        return sourceMap.size();
    }

    @Override
    public boolean isEmpty() {
        return sourceMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return sourceMap.containsKey(key) || key.equals("get");
    }

    @Override
    public boolean containsValue(Object value) {
        return sourceMap.containsValue(value);
    }

    @Override
    public TemplateModel get(Object key) {
        TemplateModel o = sourceMap.get(key);
        if(o == null && key.equals("get")) {
            o = (TemplateMethodModelEx) list -> {
                if(list.size() == 0 || list.size() > 2)
                    throw new TemplateModelException("Wrong number of arguments");
                Object o1 = list.get(0);
                if(!(o1 instanceof TemplateScalarModel))
                    throw new TemplateModelException("Argument 0 cannot be converted to a string!");
                String asString = ((TemplateScalarModel) o1).getAsString();
                String defaultString = asString;
                if(list.size() == 2) {
                    Object o2 = list.get(1);
                    if(!(o2 instanceof TemplateScalarModel))
                        throw new TemplateModelException("Argument 1 cannot be converted to a string!");
                    defaultString = ((TemplateScalarModel) o2).getAsString();
                }
                return sourceMap.getOrDefault(asString, wrapper.wrap(defaultString));
            };
        }
        return o;
    }

    @Override
    public TemplateModel put(String key, TemplateModel value) {
        throw new UnsupportedOperationException("DataTemplateMap is unmodifiable!");
    }

    @Override
    public TemplateModel remove(Object key) {
        throw new UnsupportedOperationException("DataTemplateMap is unmodifiable!");
    }

    @Override
    public void putAll(Map<? extends String, ? extends TemplateModel> m) {
        throw new UnsupportedOperationException("DataTemplateMap is unmodifiable!");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("DataTemplateMap is unmodifiable!");
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<TemplateModel> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<String, TemplateModel>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
