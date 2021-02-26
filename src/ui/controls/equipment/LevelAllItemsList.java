package ui.controls.equipment;

import javafx.scene.control.TreeTableColumn;
import model.items.Item;
import model.player.PC;
import model.xml_parsers.equipment.ItemLoader;
import ui.controls.lists.entries.ItemEntry;
import ui.controls.lists.factories.TreeCellFactory;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class LevelAllItemsList extends AllItemsList<Integer, String> {

    public LevelAllItemsList(PC character, BiConsumer<Item, Integer> handler) {
        super(character, handler, Item::getLevel, Item::getCategory);
    }

    public LevelAllItemsList(PC character, BiConsumer<Item, Integer> handler, Function<Item, Item> transformer) {
        super(character, handler, Item::getLevel, Item::getCategory, transformer);
    }

    @Override
    protected void createColumns() {
        TreeTableColumn<ItemEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<ItemEntry, String> cost = new TreeTableColumn<>("Cost");
        TreeTableColumn<ItemEntry, String> subCat = new TreeTableColumn<>("Subcategory");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        cost.setCellValueFactory(new TreeCellFactory<>("cost"));
        cost.setStyle( "-fx-alignment: CENTER;");
        subCat.setCellValueFactory(new TreeCellFactory<>("subCategory"));
        subCat.setStyle( "-fx-alignment: CENTER;");
        cost.setComparator(Comparator.comparingDouble(ItemLoader::getPrice));
        //noinspection unchecked
        this.getColumns().addAll(name, cost, subCat);
        name.minWidthProperty().bind(this.widthProperty().multiply(.5));
    }

}
