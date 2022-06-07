package model.data_managers.sources;

import model.xml_parsers.ChoicesLoader;
import model.xml_parsers.FileLoader;

import java.util.*;
import java.util.function.Consumer;

public final class Source {
    private final String name, shortName, description, category, subCategory;
    private final List<String> dependencies;
    private final Map<Class<? extends FileLoader<?>>, FileLoader<?>> loaders;

    private Source(Source.Builder builder) {
        name = builder.name;
        shortName = builder.shortName;
        category = builder.category;
        subCategory = builder.subCategory;
        description = builder.description;
        dependencies = builder.dependencies;
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

    public String getShortName() {
        return shortName;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    @Override
    public String toString() {
        return getName();
    }

    public <T, U extends FileLoader<T>> U getLoader(Class<U> loaderClass) {
        FileLoader<?> fileLoader = loaders.get(loaderClass);
        if(loaderClass.isInstance(fileLoader))
            return loaderClass.cast(fileLoader);
        else return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Source source = (Source) o;
        return name.equals(source.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static class Builder {
        private String name, shortName, description, category, subCategory;
        private List<String> dependencies = Collections.emptyList();
        private ChoicesLoader choices;
        private final Map<Class<? extends FileLoader<?>>, FileLoader<?>> loaders = new HashMap<>();
        private final List<Consumer<Source>> buildListeners = new ArrayList<>();
        private Source source = null;

        public void setName(String name) {
            this.name = name;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setSubCategory(String subCategory) {
            this.subCategory = subCategory;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void addDependency(String sourceName) {
            if(dependencies.size() == 0) dependencies = new ArrayList<>();
            dependencies.add(sourceName);
        }

        public <T, U extends FileLoader<T>> void addLoader(Class<U> loaderClass, U loader) {
            loaders.put(loaderClass, loader);
        }

        public Source build() {
            source = new Source(this);
            return source;
        }

        public Source onBuild(Consumer<Source> consumer) {
            if(source == null)
                buildListeners.add(consumer);
            return source;
        }
    }
}
