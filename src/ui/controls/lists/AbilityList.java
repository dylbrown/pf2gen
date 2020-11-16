package ui.controls.lists;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeTableColumn;
import model.abilities.Ability;
import ui.controls.lists.entries.AbilityEntry;
import ui.controls.lists.factories.TreeCellFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AbilityList extends ObservableCategoryEntryList<Ability, AbilityEntry> {

    public static AbilityList getLevelTypeList(ObservableList<Ability> items, BiConsumer<Ability, Integer> handler) {
        return new AbilityList(items, handler,
                ability -> "Level " + ability.getLevel(),
                ability -> ability.getType().toString() + " Feat");
    }

    public static AbilityList getTypeLevelList(ObservableList<Ability> items, BiConsumer<Ability, Integer> handler) {
        return new AbilityList(items, handler,
                ability -> ability.getType().toString() + " Feat",
                ability -> "Level " + ability.getLevel());
    }

    public AbilityList(ObservableList<Ability> items,
                       BiConsumer<Ability, Integer> handler,
                       Function<Ability, String> getCategory,
                       Function<Ability, String> getSubCategory) {
        super(items, handler, getCategory, getSubCategory,
                AbilityEntry::new, AbilityEntry::new, AbilityList::makeColumns);
        getSortOrder().add(getColumns().get(0));
    }

    private static List<TreeTableColumn<AbilityEntry, ?>> makeColumns(ReadOnlyDoubleProperty width) {
        TreeTableColumn<AbilityEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<AbilityEntry, String> level = new TreeTableColumn<>("Level");
        TreeTableColumn<AbilityEntry, String> type = new TreeTableColumn<>("Type");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        name.minWidthProperty().bind(width.multiply(.6));
        level.setCellValueFactory(new TreeCellFactory<>("level"));
        level.setStyle( "-fx-alignment: CENTER;");
        name.setComparator((s1,s2)->{
            if(s1.matches("Level \\d+") && s2.matches("Level \\d+")) {
                double d1 = Double.parseDouble(s1.substring(6));
                double d2 = Double.parseDouble(s2.substring(6));
                return Double.compare(d1, d2);
            }
            return s1.compareTo(s2);
        });
        level.setComparator((s1,s2)->{
            double d1 = (s1.length() >= 6)? Double.parseDouble(s1.substring(6)) : 0;
            double d2 = (s1.length() >= 6)? Double.parseDouble(s2.substring(6)) : 0;
            return Double.compare(d1, d2);
        });
        type.setCellValueFactory(new TreeCellFactory<>("type"));
        type.setStyle( "-fx-alignment: CENTER;");
        return Arrays.asList(name, level, type);
    }
}
