package concurrent;

import functions.TabulatedFunction;

public class MultiplyingTask implements Runnable {
    private final TabulatedFunction function;

    public MultiplyingTask(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public void run() {
        synchronized (function) {
            for (int i = 0; i < function.getCount(); i++) {
                double currentY = function.getY(i);
                function.setY(i, currentY * 2);
            }
        }
        System.out.println(Thread.currentThread().getName() + " finished multiplying");
    }
}