package model.player;

public interface PlayerState {
    /**
     * This resets all the state which is independently handled by this manager, and
     * which will not be cleared by resetting another manager
     * @param resetEvent this prevents this public method being called by whoever wants
     */
    void reset(PC.ResetEvent resetEvent);
}
