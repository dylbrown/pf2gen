package model.equipment;

public class UnmodifiableItemCount extends ItemCount {
    public UnmodifiableItemCount(ItemCount source) {
        super(source);
    }

    public UnmodifiableItemCount(ItemCount ic, int i) {
        super(ic, i);
    }

    @Override
    public void add(int e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(int e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCount(int count) {
        throw new UnsupportedOperationException();
    }
}
