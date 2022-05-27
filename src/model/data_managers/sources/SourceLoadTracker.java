package model.data_managers.sources;

import model.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

import static model.util.StringUtils.clean;

public class SourceLoadTracker {
    private final SourceConstructor constructor;
    private Set<String> isLoaded = new HashSet<>();
    private boolean allLoaded = false;
    private Source source = null;

    public SourceLoadTracker(SourceConstructor constructor, Source.Builder sourceBuilder) {
        this.constructor = constructor;
        if(sourceBuilder != null) {
            source = sourceBuilder.onBuild((source)->this.source = source);
        }
    }

    public boolean isNotAllLoaded() {
        if(!constructor.isSingleFile())
            return isLoaded.size() != constructor.getLocations().size();
        return !allLoaded;
    }

    public boolean isNotLoaded(String name) {
        if(constructor == null || name == null)
            return false;
        switch (constructor.getType()) {
            case SingleFileMultiItem:
            case SingleFileSingleItem:
                return !allLoaded;
            case MultiFileSingleItem:
            case MultiItemMultiFile:
                return !isLoaded.contains(StringUtils.clean(name));
        }
        return true;
    }

    public void setLoaded(String location) {
        if(source != null)
            System.out.println("Loaded " + source.getShortName() + "/" + location);
        else
            System.out.println("Loaded " + location);
        if(constructor.isSingleFile()) {
            allLoaded = true;
            return;
        }
        isLoaded.add(clean(location));
    }
}
