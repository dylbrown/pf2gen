package model.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class TransformationMap<E, F, G> implements Map<E, G> {

    private final Map<E, F> sourceMap;
    private final Function<F, G> transformation;

    public TransformationMap(Map<E, F> sourceMap, Function<F, G> transformation) {
        this.sourceMap = sourceMap;
        this.transformation = transformation;
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
        return sourceMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (F f : sourceMap.values()) {
            if(transformation.apply(f).equals(value))
                return true;
        }
        return false;
    }

    @Override
    public G get(Object key) {
        return transformation.apply(sourceMap.get(key));
    }

    @Override
    public G put(E key, G value) {
        throw new UnsupportedOperationException("TransformationMap is unmodifiable!");
    }

    @Override
    public G remove(Object key) {
        throw new UnsupportedOperationException("TransformationMap is unmodifiable!");
    }

    @Override
    public void putAll(Map<? extends E, ? extends G> m) {
        throw new UnsupportedOperationException("TransformationMap is unmodifiable!");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("TransformationMap is unmodifiable!");
    }

    @Override
    public Set<E> keySet() {
        return sourceMap.keySet();
    }

    @Override
    public Collection<G> values() {
        throw new UnsupportedOperationException("Don't use this, it's expensive!");
    }

    @Override
    public Set<Entry<E, G>> entrySet() {
        throw new UnsupportedOperationException("Don't use this, it's expensive!");
    }
}
