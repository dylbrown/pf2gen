package model.xml_parsers;

import model.equipment.Equipment;

import java.util.List;

public class ItemLoader<T extends Equipment> extends FileLoader<T> {
    @Override
    public List<T> parse() {
        return null;
    }




    static double getPrice(String priceString) {
        String[] split = priceString.split(" ");
        double value = Double.parseDouble(split[0]);
        switch(split[1].toLowerCase()) {
            case "cp":
                value *= .1;
                break;
            case "gp":
                value *= 10;
                break;
            case "pp":
                value *= 100;
                break;
        }
        return value;
    }
}
