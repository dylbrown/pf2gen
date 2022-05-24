package model.data_managers.sources;

import model.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SourceConstructor {
    private final Type type;
    private Map<String, List<String>> locationMap;
    private String location;
    private final boolean multiplePerFile, singleFile;

    public SourceConstructor(Map<String, List<String>> map, boolean isMultiMulti) {
        locationMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            locationMap.put(StringUtils.clean(entry.getKey()), entry.getValue());
        }

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

    public List<String> getLocation(String name) {
        if(type == Type.SingleFileMultiItem) {
            String location = getLocation();
            return (location != null) ? Collections.singletonList(location) : Collections.emptyList();
        }
        List<String> strings = locationMap.get(StringUtils.clean(name));
        return (strings != null) ? strings : Collections.emptyList();
    }

    private List<String> locations = null;
    public List<String> getLocations() {
        if(type == Type.SingleFileMultiItem) return Collections.singletonList(getLocation());
        if(locations == null) {
            locations = locationMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        }
        return locations;
    }

    public Map<String, List<String>> map() {
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
