package model.data_managers.sources;

import java.util.HashSet;
import java.util.Set;

import static model.util.StringUtils.clean;

public class SourceLoadTracker {
    private final SourceConstructor constructor;
    private Set<String> isLoaded = new HashSet<>();
    private boolean allLoaded = false;

    public SourceLoadTracker(SourceConstructor constructor) {
        this.constructor = constructor;
    }

    public boolean isNotAllLoaded() {
        if(!constructor.isSingleFile())
            return isLoaded.size() != constructor.getLocations().size();
        return !allLoaded;
    }

    public boolean isNotLoaded(String name) {
        switch (constructor.getType()) {
            case SingleFileMultiItem:
            case SingleFileSingleItem:
                return !allLoaded;
            case MultiFileSingleItem:
                return !isLoaded.contains(name);
            case MultiItemMultiFile:
                return isNotAllLoaded();
        }
        return true;
    }

    public void setLoaded(String location) {
        System.out.println("Loaded " + location);
        if(constructor.isSingleFile()) {
            allLoaded = true;
            return;
        }
        isLoaded.add(clean(location));
    }
}
