package concurrent;

import functions.TabulatedFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WriteTask implements Runnable {
    private static final Logger logger = LogManager.getLogger(WriteTask.class);
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
        logger.info("Запуск WriteTask в потоке: {}, количество точек: {}, значение для записи: {}", 
            Thread.currentThread().getName(), function.getCount(), value);
        for (int i = 0; i < function.getCount(); i++) {
            synchronized (monitor) {
                function.setY(i, value);
                logger.debug("Записано значение {} в точку с индексом {}", value, i);
                System.out.printf("Writing for index %d complete%n", i);
            }
        }
        logger.info("WriteTask завершена в потоке: {}", Thread.currentThread().getName());
    }
}