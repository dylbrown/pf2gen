package model.player;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QualityManager {
    private final Map<String, StringProperty> qualities = new HashMap<>();

    public String get(String quality) {
        StringProperty property = qualities.get(quality.toLowerCase());
        return (property != null) ? property.get() : "";
    }

    public void set(String quality, String value) {
        getProperty(quality).set(value);
    }

    public StringProperty getProperty(String quality) {
        return qualities.computeIfAbsent(quality.toLowerCase(), s->new SimpleStringProperty(""));
    }

    public Map<String, StringProperty> map() {
        return Collections.unmodifiableMap(qualities);
    }
}
