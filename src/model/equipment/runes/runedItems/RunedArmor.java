package model.equipment.runes.runedItems;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.attributes.AttributeBonus;
import model.equipment.armor.Armor;
import model.equipment.runes.ArmorRune;

public class RunedArmor extends Armor implements RunedEquipment<ArmorRune> {
    private final Runes<ArmorRune> runes;
    private final Armor baseArmor;
    private final ObservableList<AttributeBonus> bonuses = FXCollections.observableArrayList();

    public RunedArmor(Armor armor) {
        super(new Armor.Builder(armor));
        this.baseArmor = armor;
        bonuses.addAll(armor.getBonuses());
        runes = new Runes<>(armor.getName(), ArmorRune.class);
        runes.addListener(c->{
            if(c.wasAdded()) {
                bonuses.addAll(c.getValueAdded().getBonuses());
            }
            if(c.wasRemoved()) {
                bonuses.removeAll(c.getValueRemoved().getBonuses());
            }
        });
    }

    @Override
    public Armor getBaseItem() {
        return baseArmor;
    }

    @Override
    public double getValue() {
        return super.getValue() + runes.getValue();
    }

    @Override
    public String getName() {
        return runes.makeRuneName(super.getName());
    }

    @Override
    public Runes<ArmorRune> getRunes() {
        return this.runes;
    }

    @Override
    public int getAC() {
        return runes.getAll().stream()
                .map(ArmorRune::getBonusAC)
                .reduce(super.getAC(), Integer::sum);
    }


    @Override
    public ObservableList<AttributeBonus> getBonuses() {
        return bonuses;
    }

}
