package model.util;

import java.lang.reflect.Constructor;
import java.util.*;

public abstract class Copy {
    public static <T> T copy(T source) {
        Class<?> tClass = source.getClass();
        try {
            Constructor<?> constructor = tClass.getDeclaredConstructor(source.getClass());
            //noinspection unchecked
            return (T) constructor.newInstance(source);
        } catch (Exception e) {
            return source;
        }
    }
    public static <T> List<T> copy(List<T> source) {
        List<T> newList = new ArrayList<>();
        for (T t : source) {
            newList.add(copy(t));
        }
        return newList;
    }

    public static <T, U> Map<T, U> shallowCopyMap(Map<T, U> map) {
        if(map.size() == 0) return Collections.emptyMap();
        return new HashMap<>(map);
    }

    public static <T, U> Map<T, U> copyMap(Map<T, U> map) {
        if(map.size() == 0) return Collections.emptyMap();
        Map<T, U> newMap = new HashMap<>();
        for (Map.Entry<T, U> entry : map.entrySet()) {
            newMap.put(entry.getKey(), copy(entry.getValue()));
        }
        return newMap;
    }
}
