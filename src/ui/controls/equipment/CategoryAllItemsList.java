package ui.controls.equipment;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.TreeTableColumn;
import model.items.Item;
import model.player.PC;
import model.xml_parsers.equipment.ItemLoader;
import ui.controls.lists.entries.ItemEntry;
import ui.controls.lists.factories.TreeCellFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CategoryAllItemsList extends AllItemsList<String, String> {

    public CategoryAllItemsList(PC character, BiConsumer<Item, Integer> handler) {
        super(character, handler, Item::getCategory, Item::getSubCategory);
    }

    public CategoryAllItemsList(PC character, BiConsumer<Item, Integer> handler, Function<Item, Item> transformer) {
        super(character, handler, Item::getCategory, Item::getSubCategory, transformer);
    }

    public static List<TreeTableColumn<ItemEntry, ?>> makeColumns(ReadOnlyDoubleProperty width) {
        TreeTableColumn<ItemEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<ItemEntry, String> cost = new TreeTableColumn<>("Cost");
        TreeTableColumn<ItemEntry, String> level = new TreeTableColumn<>("Level");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        name.minWidthProperty().bind(width.multiply(.6));
        cost.setCellValueFactory(new TreeCellFactory<>("cost"));
        cost.setStyle( "-fx-alignment: CENTER;");
        level.setCellValueFactory(new TreeCellFactory<>("level"));
        level.setStyle( "-fx-alignment: CENTER;");
        cost.setComparator(Comparator.comparingDouble(ItemLoader::getPrice));
        level.setComparator((s1,s2)->{
            double d1 = (!s1.equals(""))? Double.parseDouble(s1) : 0;
            double d2 = (!s2.equals(""))? Double.parseDouble(s2) : 0;
            return Double.compare(d1, d2);
        });
        return Arrays.asList(name, cost, level);
    }

    @Override
    protected void createColumns() {
        this.getColumns().addAll(makeColumns(this.widthProperty()));
    }
}
