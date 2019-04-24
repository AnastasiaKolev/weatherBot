package alerts;

/**
 * @author anastasia.kolevatykh
 * @version 1.0
 * @brief Task to be execute periodically
 */
public abstract class CustomTimerTask {

    /**
     * @abstract Should contain the functionality of the task
     */
    public abstract void execute();
}