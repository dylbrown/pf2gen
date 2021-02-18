package model.data_managers.sources;

import java.util.HashMap;

public class TypeSourceConstructor extends SourceConstructor {
    private final HashMap<String, String> typeMap;

    public TypeSourceConstructor(HashMap<String, String> map, HashMap<String, String> typeMap, boolean isMultiMulti) {
        super(map, isMultiMulti);
        this.typeMap = typeMap;
    }

    public String getObjectType(String key) {
        return typeMap.get(key);
    }
}
