package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InterpolationException;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractTabulatedFunction implements TabulatedFunction {
    protected static final Logger logger = LogManager.getLogger(AbstractTabulatedFunction.class);
    protected int count;

    protected abstract int floorIndexOfX(double x);
    protected abstract double extrapolateLeft(double x);
    protected abstract double extrapolateRight(double x);
    protected abstract double interpolate(double x, int floorIndex);

    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        return leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX);
    }

    // проверка одинаковы ли длины массивов X и Y
    public static void checkLengthIsTheSame(double[] xValues, double[] yValues) {
        logger.debug("Проверка длины массивов: xValues.length={}, yValues.length={}", xValues.length, yValues.length);
        if (xValues.length != yValues.length) {
            logger.error("Массивы имеют разную длину: xValues.length={}, yValues.length={}", xValues.length, yValues.length);
            throw new DifferentLengthOfArraysException("Массивы имеют разную длину");
        }
        logger.debug("Проверка длины массивов пройдена успешно");
    }

    // проверка отсортирован ли массив X по возрастанию
    public static void checkSorted(double[] xValues) {
        logger.debug("Проверка отсортированности массива X, длина: {}", xValues.length);
        for (int i = 1; i < xValues.length; i++) {
            // Каждый следующий X должен быть строго больше предыдущего
            if (xValues[i] <= xValues[i - 1]) {
                logger.error("Массив X не отсортирован по возрастанию: xValues[{}]={} <= xValues[{}]={}", 
                    i, xValues[i], i-1, xValues[i-1]);
                throw new ArrayIsNotSortedException("Массив X не отсортирован по возрастанию");
            }
        }
        logger.debug("Проверка отсортированности массива X пройдена успешно");
    }

    @Override
    public double apply(double x) {
        logger.debug("Вычисление значения функции для x={}, границы: [{}, {}]", x, leftBound(), rightBound());
        // если x левее левой границы - экстраполяция слева
        if (x < leftBound()) {
            logger.debug("x={} меньше левой границы {}, выполняется экстраполяция слева", x, leftBound());
            double result = extrapolateLeft(x);
            logger.debug("Результат экстраполяции слева: f({})={}", x, result);
            return result;
        }
        // если x правее правой границы - экстраполяция справа
        else if (x > rightBound()) {
            logger.debug("x={} больше правой границы {}, выполняется экстраполяция справа", x, rightBound());
            double result = extrapolateRight(x);
            logger.debug("Результат экстраполяции справа: f({})={}", x, result);
            return result;
        }
        // если x внутри границ
        else {
            int index = indexOfX(x);
            // если x точно совпадает с одним из узлов - возвращаем соответствующий Y
            if (index != -1) {
                double result = getY(index);
                logger.debug("x={} точно совпадает с узлом индекса {}, возвращается y={}", x, index, result);
                return result;
            }
            // если x между узлами - интерполяция
            else {
                int floorIndex = floorIndexOfX(x);
                logger.debug("x={} находится между узлами, floorIndex={}", x, floorIndex);

                // проверка, x должен находиться между floorIndex и floorIndex+1
                double leftX = getX(floorIndex);
                double rightX = getX(floorIndex + 1);
                if (x < leftX || x > rightX) {
                    logger.error("x={} находится вне интервала интерполяции: [{}, {}]", x, leftX, rightX);
                    throw new InterpolationException("x находится вне интервала интерполяции");
                }

                double result = interpolate(x, floorIndex);
                logger.debug("Результат интерполяции: f({})={} (между узлами [{}, {}] и [{}, {}])", 
                    x, result, leftX, getY(floorIndex), rightX, getY(floorIndex + 1));
                return result;
            }
        }
    }

    @Override
    public Iterator<Point> iterator() {
        throw new UnsupportedOperationException("Итератор пока не реализован");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // название класса и размер
        sb.append(getClass().getSimpleName())
                .append(" size = ")
                .append(count)
                .append("\n");

        // Перечисление точек
        for (Point point : this) {
            sb.append("[")
                    .append(point.x)
                    .append("; ")
                    .append(point.y)
                    .append("]\n");
        }

        return sb.toString();
    }
}