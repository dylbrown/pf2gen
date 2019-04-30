package model.enums;

public enum Action {
    Free(-1), Reaction(0), One(1), Two(2), Three(3);

    private final int cost;

    Action(int cost) {
        this.cost = cost;
    }

    public static Action robustValueOf(String s) {
        Action result = null;
        try{
            result = valueOf(s);
        }catch (Exception e) {
            switch (s) {
                case "1":
                    result = One;
                    break;
                case "2":
                    result = Two;
                    break;
                case "3":
                    result = Three;
                    break;
            }
        }
        return result;
    }

    public int getCost() {
        return cost;
    }
}
