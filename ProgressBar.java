import static java.lang.System.out;

/**
 * Prints and controls a progress bar in the terminal. To know more info:
 * https://github.com/FlyingWolFox/Duplicate-Finder
 * 
 * @author FlyingWolFox
 * @version 1.0-beta
 */
public class ProgressBar {
    private String taskName;
    private long max;
    private long current;
    private long oldPercentage;
    private long oldProgress;

    public ProgressBar(String taskName, long maxValue) {
        this.taskName = taskName;
        this.max = maxValue;
        current = 0;
    }

    private void print(boolean end) {
        long percentage;
        long progress;
        percentage = (long) (100 * ((double) current / max));
        progress = (long) (50 * (double) percentage) / 100;
        if (percentage == oldPercentage && progress == oldProgress)
            return;
        oldPercentage = percentage;
        oldProgress = progress;
        out.print("\r");
        out.print(taskName + " ");
        out.print(percentage + "% ");
        out.print("[");
        for (int i = 0; i < progress; i++)
            out.print("=");
        for (int i = 0; i < (50 - progress); i++)
            out.print(" ");
        out.print("]");
        if (end)
            out.print("\n");
    }

    public void update() {
        boolean end = false;
        current++;
        if (current == max)
            end = true;
        print(end);
    }
}