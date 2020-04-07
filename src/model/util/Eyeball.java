package model.util;

import java.util.ArrayList;
import java.util.List;


public class Eyeball<T,U> {
    private final U owner;

    public Eyeball(U owner) {
        this.owner = owner;
    }

    private List<Watcher<T,U>> watchers = new ArrayList<>();

    public void addWatcher(Watcher<T,U> watcher) {
        watchers.add(watcher);
    }

    public void wink(T oldVal, T newVal) {
        for (Watcher<T,U> watcher : watchers) {
            watcher.react(owner, oldVal, newVal);
        }
    }
}