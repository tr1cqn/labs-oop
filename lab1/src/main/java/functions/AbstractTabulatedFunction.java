package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InterpolationException;
import java.util.Iterator;

public abstract class AbstractTabulatedFunction implements TabulatedFunction {
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
        if (xValues.length != yValues.length) {
            throw new DifferentLengthOfArraysException("Массивы имеют разную длину");
        }
    }

    // проверка отсортирован ли массив X по возрастанию
    public static void checkSorted(double[] xValues) {
        for (int i = 1; i < xValues.length; i++) {
            // Каждый следующий X должен быть строго больше предыдущего
            if (xValues[i] <= xValues[i - 1]) {
                throw new ArrayIsNotSortedException("Массив X не отсортирован по возрастанию");
            }
        }
    }

    @Override
    public double apply(double x) {
        // если x левее левой границы - экстраполяция слева
        if (x < leftBound()) {
            return extrapolateLeft(x);
        }
        // если x правее правой границы - экстраполяция справа
        else if (x > rightBound()) {
            return extrapolateRight(x);
        }
        // если x внутри границ
        else {
            int index = indexOfX(x);
            // если x точно совпадает с одним из узлов - возвращаем соответствующий Y
            if (index != -1) {
                return getY(index);
            }
            // если x между узлами - интерполяция
            else {
                int floorIndex = floorIndexOfX(x);

                // проверка, x должен находиться между floorIndex и floorIndex+1
                double leftX = getX(floorIndex);
                double rightX = getX(floorIndex + 1);
                if (x < leftX || x > rightX) {
                    throw new InterpolationException("x находится вне интервала интерполяции");
                }

                return interpolate(x, floorIndex);
            }
        }
    }

    @Override
    public Iterator<Point> iterator() {
        throw new UnsupportedOperationException("Итератор пока не реализован");
    }
}