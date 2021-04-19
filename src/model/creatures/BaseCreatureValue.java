package model.creatures;

public class BaseCreatureValue<T> implements CreatureValue<T> {
    private final T target;
    private final int modifier;
    private final String info;

    private BaseCreatureValue(Builder<T> builder) {
        this.target = builder.target;
        this.modifier = builder.modifier;
        this.info = builder.info;
    }

    @Override
    public T getTarget() {
        return target;
    }

    @Override
    public int getModifier() {
        return modifier;
    }

    @Override
    public String getInfo() {
        return info;
    }

    public static class Builder<T> {
        private final T target;
        public int modifier;
        public String info;

        public Builder(T target) {
            this.target = target;
        }

        public T getTarget() {
            return target;
        }

        public BaseCreatureValue<T> build() {
            return new BaseCreatureValue<>(this);
        }
    }
}
