package ui.controls;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;
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

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;


public class SelectionPane<T, U extends ListEntry<T>> extends BorderPane {

    protected final Choice<T> choice;
    protected final WebView display;
    protected Function<T, String> generator;
    protected final ObservableValue<Pair<Function<T, String>, String>> categoryFunctionProperty;
    protected final ObservableValue<Pair<Function<T, String>, String>> subCategoryFunctionProperty;
    protected final Function<T, U> makeEntry;
    protected final Function<String, U> makeLabelEntry;
    protected final ObservableList<T> list;
    private final Map<Pair<String, String>, ObservableCategoryEntryList<T, U>> entryListMap = new HashMap<>();
    private ObservableCategoryEntryList<T, U> currentList = null;

    protected SelectionPane(SelectionPane.Builder<T, U> builder) {
        this.choice = builder.choice;
        this.display = builder.display;
        this.categoryFunctionProperty = builder.categoryFunctionProperty;
        this.subCategoryFunctionProperty = builder.subCategoryFunctionProperty;
        if(choice != null)
            this.generator = HTMLGenerator.getGenerator(choice.getOptionsClass());
        this.makeEntry = builder.makeEntry;
        this.makeLabelEntry = builder.makeLabelEntry;
        ObservableList<T> tempList = builder.options;
        if(choice != null)
            tempList = new FilteredSelectionList<>(tempList, choice);
        FilteredList<T> searchFilter = new FilteredList<>(tempList, getFilter(builder.search));
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
        Pair<Function<T, String>, String> getCategory = categoryFunctionProperty.getValue();
        Pair<Function<T, String>, String> getSubCategory = subCategoryFunctionProperty.getValue();
        ObservableCategoryEntryList<T, U> list = entryListMap.computeIfAbsent(
                new Pair<>(getCategory.second, getSubCategory.second), p->makeList());
        currentList = list;
        setCenter(list);
    }

    protected ObservableCategoryEntryList<T, U> makeList() {
        return new ObservableCategoryEntryList<>(
                        this.list,
                        (item, clickCount)->{
                            setDisplay(choice, item);
                            if(clickCount != 2) return;
                            choice.add(item);
                        },
                        categoryFunctionProperty.getValue().first,
                        subCategoryFunctionProperty.getValue().first,
                        makeEntry, makeLabelEntry,
                        this::makeColumns);
    }



    protected <X> void setDisplay(Choice<X> choice, Object chosenValue) {
        Function<X, String> generator = HTMLGenerator.getGenerator(choice.getOptionsClass());
        if(chosenValue != null) {
            display.getEngine().loadContent(
                    generator.apply(choice.getOptionsClass().cast(chosenValue))
            );
        }
    }

    private Predicate<T> getFilter(StringProperty search) {
        return t->StringUtils.containsIgnoreCase(t.toString(), search.get());
    }

    protected List<TreeTableColumn<U, ?>> makeColumns(ReadOnlyDoubleProperty width) {
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

    public void expandAll() {
        if(currentList != null)
            expand(currentList.getRoot());
    }

    private void expand(TreeItem<U> root) {
        List<TreeItem<U>> parents = new ArrayList<>();
        TreeItem<U> parent = root.getParent();
        while(parent != null) {
            parents.add(parent);
            parent = parent.getParent();
        }
        ListIterator<TreeItem<U>> it = parents.listIterator(parents.size());
        while(it.hasPrevious())
            it.previous().setExpanded(true);
        root.setExpanded(true);
        for (TreeItem<U> child : root.getChildren()) {
            expand(child);
        }
    }

    public void navigate(List<String> path) {
        if(currentList == null)
            return;
        expandTo(path.get(0), currentList.getRoot());
    }

    private boolean expandTo(String nodeName, TreeItem<U> node) {
        if(node.getValue() != null && node.getValue().toString().equalsIgnoreCase(nodeName)) {
            expand(node);
            handleSelect(node);
            currentList.requestFocus();
            currentList.getSelectionModel().select(node);
            return true;
        }
        return node.getChildren().stream().anyMatch(child->expandTo(nodeName, child));
    }

    protected void handleSelect(TreeItem<U> node) {
        if(node.getValue() != null) {
            setDisplay(choice, node);
        }
    }

    public static class Builder<T, U extends ListEntry<T>> {
        private Choice<T> choice;
        private ObservableList<T> options;
        private WebView display;
        private StringProperty search;
        private ObservableValue<Pair<Function<T, String>, String>> categoryFunctionProperty;
        private ObservableValue<Pair<Function<T, String>, String>> subCategoryFunctionProperty;
        private final Function<T, U> makeEntry;
        private final Function<String, U> makeLabelEntry;

        public static <T> Builder<T, ListEntry<T>> makeDefault() {
            return new Builder<>(ListEntry::new, ListEntry::new);
        }

        public Builder(Function<T, U> makeEntry, Function<String, U> makeLabelEntry){
            this.makeEntry = makeEntry;
            this.makeLabelEntry = makeLabelEntry;
        }

        public void setChoice(Choice<T> choice) {
            if(choice instanceof ChoiceList)
                setOptions(makeList(((ChoiceList<T>) choice).getOptions()));
            this.choice = choice;
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

        protected ObservableList<T> getOptions() {
            return options;
        }

        public void setDisplay(WebView display) {
            this.display = display;
        }

        public void setSearch(StringProperty search) {
            this.search = search;
        }

        public void setCategoryFunctionProperty(ObservableValue<Pair<Function<T, String>, String>> categoryFunctionProperty) {
            this.categoryFunctionProperty = categoryFunctionProperty;
        }

        public void setSubCategoryFunctionProperty(ObservableValue<Pair<Function<T, String>, String>> subCategoryFunctionProperty) {
            this.subCategoryFunctionProperty = subCategoryFunctionProperty;
        }

        protected void checkNulls() {
            if(categoryFunctionProperty == null)
                categoryFunctionProperty = new SimpleObjectProperty<>(new Pair<>((t)->"", ""));
            if(subCategoryFunctionProperty == null)
                subCategoryFunctionProperty = new SimpleObjectProperty<>(new Pair<>((t)->"", ""));
        }

        public SelectionPane<T, U> build() {
            checkNulls();
            return new SelectionPane<>(this);
        }
    }
}
