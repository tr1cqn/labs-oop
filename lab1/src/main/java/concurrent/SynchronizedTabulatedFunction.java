package concurrent;

import functions.TabulatedFunction;
import functions.Point;
import java.util.Iterator;
import operations.TabulatedFunctionOperationService;
import java.util.NoSuchElementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SynchronizedTabulatedFunction implements TabulatedFunction {
    private static final Logger logger = LogManager.getLogger(SynchronizedTabulatedFunction.class);
    private final TabulatedFunction function;

    public SynchronizedTabulatedFunction(TabulatedFunction function) {
        this.function = function;
        logger.debug("Создан SynchronizedTabulatedFunction для функции типа: {}, количество точек: {}", 
            function.getClass().getSimpleName(), function.getCount());
    }

    public interface Operation<T> {
        T apply(SynchronizedTabulatedFunction function);
    }

    @Override
    public synchronized int getCount() {
        return function.getCount();
    }

    @Override
    public synchronized double getX(int index) {
        return function.getX(index);
    }

    @Override
    public synchronized double getY(int index) {
        return function.getY(index);
    }

    @Override
    public synchronized void setY(int index, double value) {
        function.setY(index, value);
    }

    @Override
    public synchronized int indexOfX(double x) {
        return function.indexOfX(x);
    }

    @Override
    public synchronized int indexOfY(double y) {
        return function.indexOfY(y);
    }

    @Override
    public synchronized double leftBound() {
        return function.leftBound();
    }

    @Override
    public synchronized double rightBound() {
        return function.rightBound();
    }

    @Override
    public synchronized double apply(double x) {
        return function.apply(x);
    }

    @Override
    public synchronized Iterator<Point> iterator() {
        logger.debug("Создание итератора для SynchronizedTabulatedFunction");
        // В блоке синхронизации создаем копию данных
        Point[] pointsCopy = TabulatedFunctionOperationService.asPoints(function);
        logger.debug("Создана копия из {} точек для итератора", pointsCopy.length);

        // Возвращаем анонимный итератор, работающий с копией
        return new Iterator<Point>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < pointsCopy.length;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    logger.warn("Попытка получить следующий элемент из итератора, когда элементов больше нет");
                    throw new NoSuchElementException("No more elements in iterator");
                }
                return pointsCopy[currentIndex++];
            }
        };
    }
    public synchronized <T> T doSynchronously(Operation<? extends T> operation) {
        logger.debug("Выполнение синхронной операции над SynchronizedTabulatedFunction");
        T result = operation.apply(this);
        logger.debug("Синхронная операция завершена");
        return result;
    }

}