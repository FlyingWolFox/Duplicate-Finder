import static java.lang.System.out;

public class ProgressBar {
    private String taskName;
    private long max;
    private long current;

    public ProgressBar(String taskName, long maxValue) {
        this.taskName = taskName;
        this.max = maxValue;
        current = 0;
    }

    private void print(boolean end) {
        int percentage;
        int progress;
        percentage = (int) (100 * (current / max));
        progress = (50 * percentage)/100;
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