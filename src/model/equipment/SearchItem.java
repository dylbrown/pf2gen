package model.equipment;

import model.enums.Rarity;
import model.enums.Slot;

public class SearchItem extends Equipment {

    public SearchItem(String name) {
        super(new SearchItem.Builder(name));
    }

    private static class Builder extends Equipment.Builder {
        public Builder(String name) {
            setName(name);
        }
    }

    @Override
    public Equipment copy() {
        return new SearchItem(getName());
    }

    @Override
    public double getWeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rarity getRarity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrettyWeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Slot getSlot() {
        throw new UnsupportedOperationException();
    }
}
