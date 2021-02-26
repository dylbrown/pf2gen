package model.data_managers.sources;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import model.util.ObjectNotFoundException;
import model.util.StringUtils;
import model.xml_parsers.FileLoader;

import java.util.*;

public class MultiSourceLoader<T> {
    protected final ObservableMap<String, T> allItems = FXCollections.observableMap(new TreeMap<>());
    protected final NavigableMap<String, ObservableMap<String, T>> categorizedItems = new TreeMap<>();
    protected final List<FileLoader<T>> loaders = new ArrayList<>();
    private final String type;
    protected Set<String> categories;
    protected boolean notLoaded = true;
    public MultiSourceLoader(String type) {
        this.type = type;
    }

    public void add(FileLoader<T> loader) {
        loaders.add(loader);
        if(!notLoaded) {
            for (Map.Entry<String, T> entry : loader.getAll().entrySet()) {
                allItems.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public ObservableMap<String, T> getAll() {
        if(notLoaded) {
            for (FileLoader<T> loader : loaders) {
                for (Map.Entry<String, T> entry : loader.getAll().entrySet()) {
                    allItems.put(entry.getKey(), entry.getValue());
                }
            }
            notLoaded = false;
        }
        return FXCollections.unmodifiableObservableMap(allItems);
    }

    public T find(String name) throws ObjectNotFoundException {
        for (FileLoader<T> loader : loaders) {
            T t = loader.find(name);
            if(t != null)
                return t;
        }
        throw new ObjectNotFoundException(name, type);
    }

    public T find(String category, String name) throws ObjectNotFoundException {
        for (FileLoader<T> loader : loaders) {
            T t = loader.find(category, name);
            if(t != null)
                return t;
        }
        throw new ObjectNotFoundException(name, category, type);
    }

    public ObservableMap<String, T> getCategory(String category) {
        category = StringUtils.clean(category);
        if(categorizedItems.get(category) == null) {
            ObservableMap<String, T> map = FXCollections.observableMap(new TreeMap<>());
            for (FileLoader<T> loader : loaders) {
                for (Map.Entry<String, T> entry : loader.getCategory(category).entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            categorizedItems.put(StringUtils.clean(category), map);
        }
        return FXCollections.unmodifiableObservableMap(categorizedItems.get(StringUtils.clean(category)));
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