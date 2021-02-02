package ui.controls;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import model.ability_slots.Choice;
import model.ability_slots.ChoiceList;
import model.util.FilteredSelectionList;
import model.util.Pair;
import model.util.StringUtils;
import ui.controls.lists.ObservableCategoryEntryList;
import ui.controls.lists.entries.ListEntry;
import ui.controls.lists.factories.TreeCellFactory;
import ui.html.HTMLGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;


public class SelectionPane<T> extends BorderPane {

    private final Choice<T> choice;
    private final WebView display;
    private final Function<T, String> generator;
    private final ObservableValue<Function<T, String>> categoryFunctionProperty;
    private final ObservableValue<Function<T, String>> subCategoryFunctionProperty;
    private final ObservableList<T> list;
    private final Map<Pair<Function<T, String>, Function<T, String>>, ObservableCategoryEntryList<T, ListEntry<T>>> entryListMap = new HashMap<>();

    protected SelectionPane(SelectionPane.Builder<T> builder) {
        this.choice = builder.choice;
        this.display = builder.display;
        this.categoryFunctionProperty = builder.categoryFunctionProperty;
        this.subCategoryFunctionProperty = builder.subCategoryFunctionProperty;
        this.generator = HTMLGenerator.getGenerator(choice.getOptionsClass());

        FilteredSelectionList<T> selectedFilter = new FilteredSelectionList<>(builder.options, choice);
        FilteredList<T> searchFilter = new FilteredList<>(selectedFilter, getFilter(builder.search));
        builder.search.addListener(c->{
            if(builder.search.get().isBlank())
                searchFilter.setPredicate(null);
            else
                searchFilter.setPredicate(getFilter(builder.search));
        });
        this.list = searchFilter;
        categoryFunctionProperty.addListener((o, oldVal, newVal)->updateGroupings());
        subCategoryFunctionProperty.addListener((o, oldVal, newVal)->updateGroupings());
        updateGroupings();
    }

    private void updateGroupings() {
        Function<T, String> getCategory = categoryFunctionProperty.getValue();
        Function<T, String> getSubCategory = subCategoryFunctionProperty.getValue();
        ObservableCategoryEntryList<T, ListEntry<T>> list = entryListMap.computeIfAbsent(new Pair<>(getCategory, getSubCategory), pair->
                new ObservableCategoryEntryList<>(
                        this.list,
                        this::handleSelect,
                        pair.first,
                        pair.second,
                        ListEntry::new, ListEntry::new,
                        this::makeColumns)
        );
        setCenter(list);
    }

    private Predicate<T> getFilter(StringProperty search) {
        return t->StringUtils.containsIgnoreCase(t.toString(), search.get());
    }

    private void handleSelect(T item, Integer clickCount) {
        display.getEngine().loadContent(generator.apply(item));
        if(clickCount != 2) return;
        choice.add(item);
    }

    private <U extends ListEntry<T>> List<TreeTableColumn<U, ?>> makeColumns(ReadOnlyDoubleProperty width) {
        TreeTableColumn<U, String> name = new TreeTableColumn<>("Name");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        name.setComparator((s1,s2)->{
            if(s1.matches("Level \\d+") && s2.matches("Level \\d+")) {
                double d1 = Double.parseDouble(s1.substring(6));
                double d2 = Double.parseDouble(s2.substring(6));
                return Double.compare(d1, d2);
            }
            return s1.compareTo(s2);
        });
        return Collections.singletonList(name);
    }

    public static class Builder<T> {
        Choice<T> choice;
        ObservableList<T> options;
        WebView display;
        StringProperty search;
        ObservableValue<Function<T, String>> categoryFunctionProperty;
        ObservableValue<Function<T, String>> subCategoryFunctionProperty;

        public void setChoice(Choice<T> choice) {
            this.choice = choice;
        }

        public void setChoice(ChoiceList<T> choice) {
            this.choice = choice;
            setOptions(makeList(choice.getOptions()));
        }
        protected static <T> ObservableList<T> makeList(List<T> options) {
            if(options instanceof ObservableList)
                return (ObservableList<T>) options;
            return FXCollections.observableArrayList(options);
        }

        public void setOptions(List<T> options) {
            setOptions(makeList(options));
        }

        public void setOptions(ObservableList<T> options) {
            this.options = options;
        }

        public void setDisplay(WebView display) {
            this.display = display;
        }

        public void setSearch(StringProperty search) {
            this.search = search;
        }

        public void setCategoryFunctionProperty(ObservableValue<Function<T, String>> categoryFunctionProperty) {
            this.categoryFunctionProperty = categoryFunctionProperty;
        }

        public void setSubCategoryFunctionProperty(ObservableValue<Function<T, String>> subCategoryFunctionProperty) {
            this.subCategoryFunctionProperty = subCategoryFunctionProperty;
        }

        public SelectionPane<T> build() {
            if(categoryFunctionProperty == null)
                categoryFunctionProperty = new SimpleObjectProperty<>((t)->"");
            if(subCategoryFunctionProperty == null)
                subCategoryFunctionProperty = new SimpleObjectProperty<>((t)->"");
            return new SelectionPane<>(this);
        }
    }
}
