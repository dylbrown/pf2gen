package ui.controls.lists;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import model.ability_slots.Choice;
import ui.controls.lists.entries.DecisionEntry;
import ui.controls.lists.factories.SelectRowFactory;
import ui.controls.lists.factories.TreeCellFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiConsumer;
//TODO: Make parent class for DecisionsList and AbstractItemList
@SuppressWarnings({"rawtypes", "unchecked"})
public class DecisionsList extends TreeTableView<DecisionEntry> {

    public DecisionsList(BiConsumer<TreeItem<DecisionEntry>, Integer> handler, ObservableList<Choice<?>> choices) {
        this.setShowRoot(false);
        TreeItem<DecisionEntry> root = new TreeItem<>(new DecisionEntry(null, "root", -1));
        this.setRoot(root);
        this.setRowFactory(new SelectRowFactory<>(Arrays.asList(handler, DecisionsList::attemptRemove)));
        this.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        createColumns();
        addItems(root, choices);
    }

    private void createColumns() {
        TreeTableColumn<DecisionEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<DecisionEntry, String> level = new TreeTableColumn<>("Level");
        TreeTableColumn<DecisionEntry, String> remaining = new TreeTableColumn<>("Remaining");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        // name.minWidthProperty().bind(this.widthProperty().multiply(.6));
        level.setCellValueFactory(new TreeCellFactory<>("level"));
        level.setStyle( "-fx-alignment: CENTER;");
        remaining.setCellValueFactory(new TreeCellFactory<>("remaining"));
        remaining.setStyle( "-fx-alignment: CENTER;");
        //noinspection unchecked
        this.getColumns().addAll(name, level, remaining);
    }

    private void addItems(TreeItem<DecisionEntry> root, ObservableList<Choice<?>> choices) {
        for (Choice<?> choice : choices) {
            TreeItem<DecisionEntry> node = new TreeItem<>(new DecisionEntry(choice));
            root.getChildren().add(node);
            for (Object o : choice.getSelections()) {
                node.getChildren().add(new TreeItem<>(new DecisionEntry(o, o.toString(), -1)));
            }

            choice.getSelections().addListener(getListener(node));
        }
        choices.addListener((ListChangeListener<Choice<?>>) c->{
            while(c.next()) {
                for (Choice<?> choice : c.getAddedSubList()) {
                    TreeItem<DecisionEntry> node = new TreeItem<>(new DecisionEntry(choice));
                    root.getChildren().add(node);
                    choice.getSelections().addListener(getListener(node));
                }
                for (Choice<?> choice : c.getRemoved()) {
                    Iterator<TreeItem<DecisionEntry>> iterator = root.getChildren().iterator();
                    while(iterator.hasNext()) {
                        TreeItem<DecisionEntry> item = iterator.next();
                        if(choice.equals(item.getValue().getChoice())){
                            iterator.remove();
                            break;
                        }
                    }
                }

            }
        });
    }

    private ListChangeListener<Object> getListener(TreeItem<DecisionEntry> node) {
        return c->{
            while(c.next()) {
                for (Object o : c.getAddedSubList()) {
                    node.getChildren().add(new TreeItem<>(new DecisionEntry(o, o.toString(), -1)));
                }
                for (Object o : c.getRemoved()) {
                    Iterator<TreeItem<DecisionEntry>> iterator = node.getChildren().iterator();
                    while(iterator.hasNext()) {
                        TreeItem<DecisionEntry> item = iterator.next();
                        if(item.getValue().getChosenValue().equals(o)){
                            iterator.remove();
                            break;
                        }
                    }
                }

            }
        };
    }

    private static void attemptRemove(TreeItem<DecisionEntry> treeItem, int count) {
        if(treeItem.getValue() == null || count % 2 != 0) return;
        Object value = treeItem.getValue().getChosenValue();
        if(value == null) return; // Haven't selected a chosen value
        Choice choice = treeItem.getParent().getValue().getChoice();
        if(choice == null) return;
        try{
            choice.remove(value);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void expandAll() {
        expand(getRoot());
    }

    private void expand(TreeItem<DecisionEntry> root) {
        root.setExpanded(true);
        for (TreeItem<DecisionEntry> child : root.getChildren()) {
            expand(child);
        }

    }
}
