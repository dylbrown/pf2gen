package model.equipment.runes.runedItems;

import model.attributes.AttributeBonus;
import model.equipment.armor.Shield;
import model.equipment.runes.ArmorRune;

import java.util.ArrayList;
import java.util.List;

public class RunedShield extends Shield implements RunedEquipment<ArmorRune> {
    private final Runes<ArmorRune> runes;
    private final List<AttributeBonus> bonuses = new ArrayList<>();
    private final Shield baseItem;

    public RunedShield(Shield armor) {
        super(new Shield.Builder(armor));
        this.baseItem = armor;
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
    public Shield getBaseItem() {
        return baseItem;
    }

    @Override
    public int getAC() {
        return runes.getAll().stream()
                .map(ArmorRune::getBonusAC)
                .reduce(super.getAC(), Integer::sum);
    }


    @Override
    public List<AttributeBonus> getBonuses() {
        return bonuses;
    }
}