package model.data_managers.sources;

import model.util.StringUtils;
import model.xml_parsers.FileLoader;

import java.util.*;

public class MultiSourceLoader<T> {
    private final NavigableMap<String, T> allItems = new TreeMap<>();
    private final NavigableMap<String, NavigableMap<String, T>> categorizedItems = new TreeMap<>();
    protected final List<? extends FileLoader<T>> loaders;
    private Set<String> categories;
    private boolean notLoaded = true;

    public MultiSourceLoader(List<? extends FileLoader<T>> loaders) {
        this.loaders = loaders;
    }

    public NavigableMap<String, T> getAll() {
        if(notLoaded) {
            for (FileLoader<T> loader : loaders) {
                for (Map.Entry<String, T> entry : loader.getAll().entrySet()) {
                    allItems.put(entry.getKey(), entry.getValue());
                }
            }
            notLoaded = false;
        }
        return Collections.unmodifiableNavigableMap(allItems);
    }

    public T find(String name) {
        for (FileLoader<T> loader : loaders) {
            T t = loader.find(name);
            if(t != null)
                return t;
        }
        return null;
    }

    public T find(String category, String name) {
        for (FileLoader<T> loader : loaders) {
            T t = loader.find(category, name);
            if(t != null)
                return t;
        }
        return null;
    }

    public NavigableMap<String, T> getCategory(String category) {
        if(categorizedItems.get(StringUtils.clean(category)) == null) {
            NavigableMap<String, T> map = new TreeMap<>();
            for (FileLoader<T> loader : loaders) {
                for (Map.Entry<String, T> entry : loader.getCategory(category).entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            categorizedItems.put(StringUtils.clean(category), map);
        }
        return Collections.unmodifiableNavigableMap(categorizedItems.get(StringUtils.clean(category)));
    }

    public Set<String> getCategories() {
        if(categories == null) {
            categories = new TreeSet<>();
            for (FileLoader<T> loader : loaders) {
                categories.addAll(loader.getCategories());
            }
        }
        return Collections.unmodifiableSet(categories);
    }
}