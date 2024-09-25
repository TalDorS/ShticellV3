package utils;

public class SleepUtils {
    public static void sleepForAWhile(long sleepTime) {
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }
    }
}

