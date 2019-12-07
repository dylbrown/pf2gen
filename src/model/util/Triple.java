package model.util;

public class Triple<T, U, V> {
    public final T first;
    public final U second;
    public final V third;

    public Triple(T t, U u, V v) {
        this.first = t;
        this.second = u;
        this.third = v;
    }


}
