package ui.controls.lists;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;

import java.util.List;
import java.util.function.BiConsumer;

public class SelectRowFactory<T> implements javafx.util.Callback<javafx.scene.control.TreeTableView<T>, javafx.scene.control.TreeTableRow<T>> {
    private final List<BiConsumer<TreeItem<T>, Integer>> handlers;

    SelectRowFactory(List<BiConsumer<TreeItem<T>, Integer>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public TreeTableRow<T> call(TreeTableView<T> item) {
        TreeTableRow<T> call = new TreeTableRow<>();
        call.setOnMouseClicked(event -> {
            TreeItem<T> treeItem = call.getTreeItem();
            if(treeItem == null) return;
            for (BiConsumer<TreeItem<T>, Integer> handler : handlers) {
                handler.accept(treeItem, event.getClickCount());
            }

        });
        return call;
    }
}
