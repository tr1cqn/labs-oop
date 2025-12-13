package concurrent;

import functions.TabulatedFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultiplyingTask implements Runnable {
    private static final Logger logger = LogManager.getLogger(MultiplyingTask.class);
    private final TabulatedFunction function;

    public MultiplyingTask(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public void run() {
        logger.info("Запуск MultiplyingTask в потоке: {}, количество точек: {}", 
            Thread.currentThread().getName(), function.getCount());
        for (int i = 0; i < function.getCount(); i++) {
            synchronized (function) {
                double currentY = function.getY(i);
                double newY = currentY * 2;
                function.setY(i, newY);
                logger.trace("Поток {} умножил значение в точке {}: {} -> {}", 
                    Thread.currentThread().getName(), i, currentY, newY);
            }
        }

        logger.info("Поток {} закончил выполнение MultiplyingTask", Thread.currentThread().getName());
        System.out.println("Поток " + Thread.currentThread().getName() + " закончил выполнение задачи");
    }
}