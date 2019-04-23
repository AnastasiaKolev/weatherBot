import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author anastasia.kolevatykh
 * @version 1.0
 * @brief Execute a task periodically
 */
public class TimerExecutor {
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1); ///< Thread to execute operations

    /**
     * Add a new CustomTimerTask to be executed
     *
     * @param task       Task to execute
     * @param targetHour Hour to execute it
     * @param targetMin  Minute to execute it
     * @param targetSec  Second to execute it
     */
    public void startExecutionEveryDayAt(CustomTimerTask task, int targetHour, int targetMin, int targetSec) {
        final Runnable taskWrapper = () -> {
            try {
                task.execute();
                startExecutionEveryDayAt(task, targetHour, targetMin, targetSec);
            } catch (Exception e) {
            }
        };

        final long delay = computeNextDelay(targetHour, targetMin, targetSec);

        executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
    }

    /**
     * Find out next daily execution
     *
     * @param targetHour Target hour
     * @param targetMin  Target minute
     * @param targetSec  Target second
     * @return time in second to wait
     */
    private long computeNextDelay(int targetHour, int targetMin, int targetSec) {
        final LocalDateTime localNow = LocalDateTime.now(Clock.systemUTC());

        LocalDateTime localNextTarget = localNow
                .withHour(targetHour)
                .withMinute(targetMin)
                .withSecond(targetSec);

        while (localNow.compareTo(localNextTarget) >= 0) {
            localNextTarget = localNextTarget.plusMinutes( 2 );
        }

        final Duration duration = Duration.between(localNow, localNextTarget);

        return duration.getSeconds();
    }
}