package model.util;

@FunctionalInterface
public interface Watcher<T,U> {
    void react(U source, T oldVal, T newVal);
}