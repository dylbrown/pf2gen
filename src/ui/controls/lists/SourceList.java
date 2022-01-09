package ui.controls.lists;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourcesLoader;
import ui.controls.lists.entries.SourceEntry;
import ui.controls.lists.factories.ThreeStateCell;
import ui.controls.lists.factories.TreeCellFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SourceList extends ObservableCategoryEntryList<Source, SourceEntry> {

    private Map<String, TreeItem<SourceEntry>> entryMap = new HashMap<>();

    public SourceList(BiConsumer<Source, Integer> handler) {
        super(FXCollections.observableArrayList(SourcesLoader.instance().getAll().values()),
                handler,
                Source::getCategory,
                Source::getSubCategory,
                SourceEntry::new,
                SourceEntry::new,
                SourceList::makeColumns
                );
        setEditable(true);
        setupDependencyChecking(getRoot());
    }

    private void setupDependencyChecking(TreeItem<SourceEntry> root) {
        for (TreeItem<SourceEntry> treeItem : root.getChildren()) {
            SourceEntry value = treeItem.getValue();
            if(value.getContents() != null) {
                String name = value.getContents().getName();
                entryMap.put(name, treeItem);
                value.stateProperty().addListener((o, oldVal, newVal)->{
                    if(newVal == ThreeState.True) {
                        addDependencies(value);
                    } else if(newVal == ThreeState.False) {
                        removeDependents(name);
                    }
                });
            }
            setupDependencyChecking(treeItem);
        }

    }

    // Recursively add dependencies
    private void addDependencies(SourceEntry addedSource) {
        for (String dependency : addedSource.getContents().getDependencies()) {
            TreeItem<SourceEntry> sourceEntry = entryMap.get(dependency);
            SourceEntry value = sourceEntry.getValue();
            if(!value.isLocked()) {
                ObjectProperty<ThreeState> p = value.stateProperty();
                if(p.get() != ThreeState.True) {
                    p.set(ThreeState.True);
                    addDependencies(value);
                    updateParent(sourceEntry.getParent(), ThreeState.True);
                }
            }
        }
    }

    // Recursively remove dependents
    private void removeDependents(String parent) {
        for (TreeItem<SourceEntry> sourceEntry : entryMap.values()) {
            if(sourceEntry.getValue().getContents().getDependencies().contains(parent)){
                if(!sourceEntry.getValue().isLocked()) {
                    ObjectProperty<ThreeState> p = sourceEntry.getValue().stateProperty();
                    if(p.get() != ThreeState.False) {
                        p.set(ThreeState.False);
                        removeDependents(sourceEntry.getValue().getContents().getName());
                        updateParent(sourceEntry.getParent(), ThreeState.False);
                    }
                }
            }
        }
    }

    private void updateParent(TreeItem<SourceEntry> parent, ThreeState value) {
        if(parent == null || parent.getValue() == null) return;
        boolean indeterminate = false;
        if(value == ThreeState.Indeterminate) {
            indeterminate = true;
        } else {
            for (TreeItem<SourceEntry> child : parent.getChildren()) {
                if(child.getValue().getState() != value) {
                    indeterminate = true;
                    break;
                }
            }
        }
        value = indeterminate ? ThreeState.Indeterminate : value;
        parent.getValue().stateProperty().set(value);
        updateParent(parent.getParent(), value);
    }

    static private List<TreeTableColumn<SourceEntry, ?>> makeColumns(ReadOnlyDoubleProperty width) {
        TreeTableColumn<SourceEntry, String> name = new TreeTableColumn<>("Name");
        name.setMinWidth(100);
        TreeTableColumn<SourceEntry, String> id = new TreeTableColumn<>("ID");
        TreeTableColumn<SourceEntry, ThreeState> enabled = new TreeTableColumn<>("Enabled?");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        id.setCellValueFactory(new TreeCellFactory<>("id"));
        id.setStyle( "-fx-alignment: CENTER;");
        enabled.setCellFactory(ThreeStateCell::new);
        enabled.setCellValueFactory(features -> features.getValue().getValue().stateProperty());
        enabled.setStyle( "-fx-alignment: CENTER;");
        return Arrays.asList(name, id, enabled);
    }

    public void selectAndLock(List<Source> preSelectedSources) {
        for (Source source : preSelectedSources) {
            TreeItem<SourceEntry> entry = entryMap.get(source.getName());
            entry.getValue().stateProperty().set(ThreeState.LockedTrue);
            entry.getValue().lock();
            ThreeStateCell.updateParent(entry.getParent(), true);
            if(entry.getParent().getValue().stateProperty().get() == ThreeState.True) {
                entry.getParent().getValue().stateProperty().set(ThreeState.LockedTrue);
                entry.getParent().getValue().lock();
            }
        }
    }
}
