package ui.controls.lists;

import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;

import java.util.function.BiConsumer;

public class SelectRowFactory<T> implements javafx.util.Callback<javafx.scene.control.TreeTableView<T>, javafx.scene.control.TreeTableRow<T>> {
    private final BiConsumer<T, Integer> handler;

    SelectRowFactory(BiConsumer<T, Integer> handler) {
        this.handler = handler;
    }

    @Override
    public TreeTableRow<T> call(TreeTableView<T> item) {
        TreeTableRow<T> call = new TreeTableRow<>();
        call.setOnMouseClicked(event -> {
            if(call.getTreeItem() == null) return;
            T eq = call.getTreeItem().getValue();
            if(eq != null)
                handler.accept(eq, event.getClickCount());
        });
        return call;
    }
}
