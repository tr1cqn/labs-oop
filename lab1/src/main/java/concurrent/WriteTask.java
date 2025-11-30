package concurrent;

import functions.TabulatedFunction;

public class WriteTask implements Runnable {
    private final TabulatedFunction function;
    private final double value;
    private final Object monitor;

    public WriteTask(TabulatedFunction function, double value, Object monitor) {
        this.function = function;
        this.value = value;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        for (int i = 0; i < function.getCount(); i++) {
            synchronized (monitor) {
                function.setY(i, value);
                System.out.printf("Writing for index %d complete%n", i);
            }
        }
    }
}