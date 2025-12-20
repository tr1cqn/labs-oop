package concurrent;

import functions.TabulatedFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadTask implements Runnable {
    private static final Logger logger = LogManager.getLogger(ReadTask.class);
    private final TabulatedFunction function;
    private final Object monitor;

    public ReadTask(TabulatedFunction function, Object monitor) {
        this.function = function;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        logger.info("Запуск ReadTask в потоке: {}, количество точек: {}", 
            Thread.currentThread().getName(), function.getCount());
        for (int i = 0; i < function.getCount(); i++) {
            synchronized (monitor) {
                double x = function.getX(i);
                double y = function.getY(i);
                logger.debug("Прочитана точка {}: x={}, y={}", i, x, y);
                System.out.printf("After read: i = %d, x = %f, y = %f%n", i, x, y);
            }
        }
        logger.info("ReadTask завершена в потоке: {}", Thread.currentThread().getName());
    }
}