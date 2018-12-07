/**
 * This is a simple class to time how long it takes to run certain pieces of code, and just makes it easier.
 */

public class Timer {
    private long startTime;

    public Timer() {
    }

    public Timer(boolean startNow) {
        if (startNow) {
            this.start();
        }
    }

    public void reset() {
        startTime = 0;
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * @return duration in ms
     */
    public long end() {
        return System.currentTimeMillis() - startTime;
    }
}
