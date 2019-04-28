package model.enums;

public enum Action {
    Free(-1), Reaction(0), One(1), Two(2), Three(3);

    private final int cost;

    Action(int cost) {
        this.cost = cost;
    }
    public int getCost() {
        return cost;
    }
}
