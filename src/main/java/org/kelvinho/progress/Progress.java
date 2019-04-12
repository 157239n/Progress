package org.kelvinho.progress;

import javax.annotation.Nonnull;
import java.util.Stack;

/**
 * This is a class to control progresses.
 * <h3>Simple usage</h3>
 * You can create a new Progress:
 * <pre>Progress progress = new Progress(0.5); // this means the progress is half way done</pre>
 * You can then query it:
 * <pre>progress.get();</pre>
 * You can set it:
 * <pre>progress.set(0.8); // this means the progress is almost done</pre>
 * You can check whether it is done or not:
 * <pre>progress.isDone();</pre>
 * <h3>Complex usage</h3>
 * Let's say that you have task A, B and C. Task A is going to make the progress go from 0 to 0.5. Task B, 0.5 to 0.7
 * and task C, 0.7 to 1.0.
 * <p>
 * Let's say that inside task A there're also task A1 from 0 to 0.2, task A2 from 0.2 to 0.25, task A3 from 0.25 to 0.5.
 * That is the same as saying A1 from 0 to 0.4, A2 from 0.4 to 0.5, A3 from 0.5 to 1 from task A's perspective.
 * How would you encode this and make things work?
 *
 * <pre>
 *     {@code
 *          Progress progress = new Progress(0.0);
 *          progress.pushRange(0.0, 0.5); // acknowledging that task A has range from 0.0 to 0.5
 *          progress.pushRange(0.0, 0.4); // acknowledging that task A1 has range from 0.0 to 0.4 from task A's perspective
 *          {
 *              // operations related to task A1
 *              progress.set(0.5); // progress is 0.5 from A1's perspective, which is 0.2 from A's perspective, and 0.1 from a global perspective
 *              // operations related to task A1
 *              progress.set(1.0); // acknowledge that task A1 is done, but the true progress is 0.2 from a global perspective
 *          }
 *          progress.popRange(); // saying that task A1 is over, we should now focus on task A
 *          progress.pushRange(0.4, 0.5); // acknowledging that task A2 has range from 0.4 to 0.5 from task A's perspective
 *          // do other operations....
 *     }
 * </pre>
 * <p>
 * Why do these kinds of convoluted operations? May be because you want to keep track of the progress of some nested
 * method, and you want the child methods to be tracked to concern only of themselves and nothing else:
 *
 * <pre>
 *     {@code
 *         Progress progress = new Progress(0.0);
 *         int iterations = 3;
 *         for (int i = 0; i < iterations; i++) {
 *             progress.pushRange(1.0 * i / iterations, 1.0 * (i + 1) / iterations);
 *             otherMethod(progress);
 *             progress.popRange();
 *         }
 *     }
 * </pre>
 * <p>
 * For example here, you want to loop a method 3 times, each time with the method setting the progress itself.
 *
 * <h3>Further tools</h3>
 * There are a few other tools that I should mention about:
 * <p>
 * You can get an ASCII image of the progress bar:
 * <pre>progress.toString(30); // 30 here is the length</pre>
 * You can tell the progress to draw itself onto the console until the progress reaches 100% (another thread must be
 * controlling the Progress object):
 * <pre>progress.yieldUntilDone();</pre>
 * You can get the integer percentage value:
 * <pre>progress.percentage();</pre>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Progress {
    private static double epsilon = 1e-12;
    private double value = 0.0, lowerBound = 0.0, upperBound = 1.0;
    private Stack<Double> lowerBounds = new Stack<>(), upperBounds = new Stack<>(); // absolute, 0 to 1

    /**
     * Creates new Progress with progress of 0.
     */
    public Progress() {
        this(0.0);
    }

    /**
     * Creates new Progress with specified Progress.
     */
    public Progress(double progress) {
        set(progress);
        pushRange(0.0, 1.0);
    }

    private double trueValue(double rangeConsciousValue) {
        return lowerBound + (upperBound - lowerBound) * rangeConsciousValue;
    }

    private double consciousValue(double trueValue) {
        return (trueValue - lowerBound) / (upperBound - lowerBound);
    }

    /**
     * Range consciously sets the progress.
     * <p>
     * For example, if range is 0.5 to 0.7, set(0.25) will set the value to 0.55.
     */
    public synchronized void set(double value) {
        this.value = trueValue(value);
    }

    /**
     * Get the range-conscious progress.
     */
    public double get() {
        return consciousValue(value);
    }

    /**
     * Get the true progress.
     */
    public double getTrueValue() {
        return value;
    }

    /**
     * Set the true progress 1.0.
     * <p>
     * This is a dangerous method, because it sets the true value, not the range-conscious value.
     */
    @Deprecated
    public void setDone() {
        value = 1.0;
    }

    public boolean isDone() {
        return value >= 1.0 - epsilon;
    }

    /**
     * Pushes the Progress into a new range. For example, push(0.8, 0.9) followed by set(0.25), get() will return 0.825.
     */
    public synchronized void pushRange(double lowerBound, double upperBound) {
        if (lowerBound < 0 || lowerBound > 1 || upperBound < 0 || upperBound > 1) {
            throw new IllegalArgumentException("Bounds must be from 0 to 1 only.");
        }
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("Lower bound can't be greater than or equal to the upper bound");
        }
        lowerBounds.push(this.lowerBound);
        upperBounds.push(this.upperBound);
        lowerBound = trueValue(lowerBound);
        this.upperBound = trueValue(upperBound);
        this.lowerBound = lowerBound;
    }

    /**
     * Returns the Progress into the previous range.
     */
    public synchronized void popRange() {
        lowerBound = lowerBounds.pop();
        upperBound = upperBounds.pop();
    }

    /**
     * Gets the true progress in the form of percentage.
     */
    public int percentage() {
        return (int) Math.round(value * 100);
    }

    /**
     * Gets the drawing of the progress bar.
     *
     * @param width the total width of the drawing
     */
    public String toString(int width) {
        if (width <= 2) {
            throw new IllegalArgumentException("Width of progress must be greater or equal to 2, because the beginning and end already have \"[\" and \"]\"");
        }
        int progressWidth = width - 2;
        String fullSymbol = "#";
        String emptySymbol = "-";
        StringBuilder answer = new StringBuilder();
        answer.append("[");
        for (int i = 0; i < progressWidth; i++) {
            if (i * 1.0 / progressWidth < value) {
                answer.append(fullSymbol);
            } else {
                answer.append(emptySymbol);
            }
        }
        answer.append("]");
        return answer.toString();
    }

    /**
     * Gets the drawing of the progress bar with a character width of 30.
     */
    public String toString() {
        return toString(30);
    }

    private void printProgress(@Nonnull String clearString, int width, int percentage) {
        System.out.print(clearString);
        System.out.print("\rProgress: " + toString(width) + " - " + percentage + "%");
    }

    /**
     * Yields the current thread until it is done. This is mainly a convenience method to display the progress.
     */
    public void yieldUntilDone() {
        System.out.println();
        int width = 30;
        String clearString;
        {
            StringBuilder stringBuilder = new StringBuilder("\r");
            for (int i = 0; i < width + 20; i++) {
                stringBuilder.append(" ");
            }
            clearString = stringBuilder.toString();
        }
        printProgress(clearString, width, 0);
        int percentage = -1;
        while (!isDone()) {
            int newPercentage = percentage();
            if (percentage != newPercentage) {
                percentage = newPercentage;
                printProgress(clearString, width, percentage);
            }
            Thread.yield();
        }
        printProgress(clearString, width, 100);
    }

    /**
     * Gets the tolerance of every Progress.
     */
    public static double tolerance() {
        return epsilon;
    }

    /**
     * Sets the tolerance of every Progress.
     */
    public static void setTolerance(double tolerance) {
        if (tolerance > 0.1) {
            throw new RuntimeException("Tolerance is supposed to be a positive number very close to zero, so that if a Progress exceeds 1 - tolerance it will be considered done. Your tolerance value is greater than 0.1. While this might be intentional, it's highly likely that something is wrong.");
        }
        epsilon = tolerance;
    }
}

