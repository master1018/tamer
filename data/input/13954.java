public class CountedTimerTaskUtils {
    private static final boolean DEBUG = false;
    public static void reschedule(Timer timer, CountedTimerTask oldTask,
                                  CountedTimerTask newTask, int oldInterval,
                                  int newInterval) {
        long now = System.currentTimeMillis();
        long lastRun = oldTask.scheduledExecutionTime();
        long expired = now - lastRun;
        if (DEBUG) {
            System.err.println("computing timer delay: "
                               + " oldInterval = " + oldInterval
                               + " newInterval = " + newInterval
                               + " samples = " + oldTask.executionCount()
                               + " expired = " + expired);
        }
        long delay = 0;
        if (oldTask.executionCount() > 0) {
            long remainder = newInterval - expired;
            delay = remainder >= 0 ? remainder : 0;
        }
        if (DEBUG) {
            System.err.println("rescheduling sampler task: interval = "
                               + newInterval
                               + " delay = " + delay);
        }
        timer.schedule(newTask, delay, newInterval);
    }
}
