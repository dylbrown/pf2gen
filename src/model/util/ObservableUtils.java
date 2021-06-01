package model.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public abstract class ObservableUtils {
    public static <T> ObservableList<T> makeList(List<T> options) {
        if(options instanceof ObservableList)
            return (ObservableList<T>) options;
        return FXCollections.observableArrayList(options);
    }
}
