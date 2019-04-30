package alerts;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TimerExecutor {
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1); ///< Thread to execute operations

    public void startExecutionEveryDayAt(CustomTimerTask task, int targetHour, int targetMin, int targetSec) {
        final Runnable taskWrapper = () -> {
            try {
                task.execute();
                startExecutionEveryDayAt(task, targetHour, targetMin, targetSec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        final long delay = computeNextDelay(targetHour, targetMin, targetSec);

        executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
    }

    //TEST MODE: Alerts send each 2 minute
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