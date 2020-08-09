package model.data_managers.sources;

import model.xml_parsers.ChoicesLoader;
import model.xml_parsers.FileLoader;

import java.util.*;
import java.util.function.Consumer;

public final class Source {
    private final String name, shortName;
    private final Map<Class<? extends FileLoader<?>>, FileLoader<?>> loaders;

    private Source(Source.Builder builder) {
        name = builder.name;
        shortName = builder.shortName;
        loaders = builder.loaders;
        if(loaders.get(ChoicesLoader.class) == null)
            loaders.put(ChoicesLoader.class, new ChoicesLoader(null, null, null));
        for (Consumer<Source> consumer : builder.buildListeners) {
            consumer.accept(this);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getShortName() {
        return shortName;
    }

    public <T, U extends FileLoader<T>> U getLoader(Class<U> loaderClass) {
        FileLoader<?> fileLoader = loaders.get(loaderClass);
        if(loaderClass.isInstance(fileLoader))
            return loaderClass.cast(fileLoader);
        else return null;
    }

    public static class Builder {
        private String name, shortName;
        private ChoicesLoader choices;
        private final Map<Class<? extends FileLoader<?>>, FileLoader<?>> loaders = new HashMap<>();
        private final List<Consumer<Source>> buildListeners = new ArrayList<>();

        public void setName(String name) {
            this.name = name;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public <T, U extends FileLoader<T>> void addLoader(Class<U> loaderClass, U loader) {
            loaders.put(loaderClass, loader);
        }

        public Source build() {
            return new Source(this);
        }

        public void onBuild(Consumer<Source> consumer) {
            buildListeners.add(consumer);
        }
    }
}
