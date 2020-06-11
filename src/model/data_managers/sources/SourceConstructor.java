package model.data_managers.sources;

import model.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class SourceConstructor {
    private final Type type;
    private Map<String, String> locationMap;
    private String location;
    private final boolean multiplePerFile, singleFile;

    public SourceConstructor(Map<String, String> map, boolean isMultiMulti) {
        this.locationMap = map;
        this.type = (isMultiMulti) ? Type.MultiItemMultiFile : Type.MultiFileSingleItem;
        multiplePerFile = isMultiMulti;
        singleFile = false;
    }

    public SourceConstructor(String location, boolean isMulti) {
        this.location = location;
        this.type = (isMulti) ? Type.SingleFileMultiItem : Type.SingleFileSingleItem;
        multiplePerFile = isMulti;
        singleFile = true;
    }

    public String getLocation(String name) {
        if(type == Type.SingleFileMultiItem) return getLocation();
        return locationMap.get(StringUtils.clean(name));
    }

    public Collection<String> getLocations() {
        if(type == Type.SingleFileMultiItem) return Collections.singletonList(getLocation());
        return Collections.unmodifiableCollection(locationMap.values());
    }

    public Map<String, String> map() {
        return Collections.unmodifiableMap(locationMap);
    }

    public enum Type{
        SingleFileSingleItem, SingleFileMultiItem, MultiFileSingleItem, MultiItemMultiFile
    }

    public Type getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public boolean isMultiplePerFile() {
        return multiplePerFile;
    }

    public boolean isSingleFile() {
        return singleFile;
    }
}
