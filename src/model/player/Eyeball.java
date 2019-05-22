package model.player;

import java.util.Observable;

class Eyeball extends Observable {
    void wink() {
        setChanged();
        notifyObservers();
    }
}
