package model.equipment.runes.runedItems;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.attributes.AttributeBonus;
import model.equipment.Item;
import model.equipment.ItemExtension;
import model.equipment.armor.Armor;
import model.equipment.runes.ArmorRune;

public class RunedArmor extends ItemExtension {
    private final Runes<ArmorRune> runes;
    private final ObservableList<AttributeBonus> bonuses = FXCollections.observableArrayList();

    public RunedArmor(Item item) {
        super(item);
        bonuses.addAll(item.getBonuses());
        runes = new Runes<>(item.getName(), ArmorRune.class);
        runes.addListener(c->{
            if(c.wasAdded()) {
                bonuses.addAll(c.getValueAdded().getItem().getBonuses());
            }
            if(c.wasRemoved()) {
                bonuses.removeAll(c.getValueRemoved().getItem().getBonuses());
            }
        });
        if(!item.hasExtension(Armor.class))
            throw new RuntimeException("RunedArmor is not Armor");
    }

    @ItemDecorator
    public double getValue(double value) {
        return value + runes.getValue();
    }

    @ItemDecorator
    public String getName(String name) {
        return runes.makeRuneName(name);
    }

    public Runes<ArmorRune> getRunes() {
        return this.runes;
    }

    @ItemDecorator
    public int getAC(int ac) {
        return runes.getAll().stream()
                .map(ArmorRune::getBonusAC)
                .reduce(ac, Integer::sum);
    }

    public ObservableList<AttributeBonus> getBonuses() {
        return bonuses;
    }

}
