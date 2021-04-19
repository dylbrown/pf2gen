package model.util;

import java.util.*;
import java.util.function.Function;

public class ListToMap<E, F> implements Map<E, F> {
    private final List<F> list;
    private final Function<F, E> keyLookup;

    public ListToMap(List<F> list, Function<F, E> keyLookup) {
        this.list = list;
        this.keyLookup = keyLookup;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) == null;
    }

    @Override
    public boolean containsValue(Object value) {
        for (F f : list) {
            if(f.equals(value))
                return true;
        }
        return false;
    }

    @Override
    public F get(Object key) {
        for (F f : list) {
            if(Objects.equals(key, keyLookup.apply(f)))
                return f;
        }
        return null;
    }

    @Override
    public F put(E key, F value) {
        throw new UnsupportedOperationException("ListToMap is unmodifiable!");
    }

    @Override
    public F remove(Object key) {
        throw new UnsupportedOperationException("ListToMap is unmodifiable!");
    }

    @Override
    public void putAll(Map<? extends E, ? extends F> m) {
        throw new UnsupportedOperationException("ListToMap is unmodifiable!");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ListToMap is unmodifiable!");
    }

    @Override
    public Set<E> keySet() {
        throw new UnsupportedOperationException("Don't use this, it's expensive!");
    }

    @Override
    public Collection<F> values() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public Set<Entry<E, F>> entrySet() {
        throw new UnsupportedOperationException("Don't use this, it's expensive!");
    }
}
