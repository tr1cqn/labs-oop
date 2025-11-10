package operations;

import functions.Point;
import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;
import functions.factory.ArrayTabulatedFunctionFactory;
import exceptions.InconsistentFunctionsException;

public class TabulatedFunctionOperationService {

    private TabulatedFunctionFactory factory;

    // Конструкторы
    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    // Геттер и сеттер для фабрики
    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    // Вложенный интерфейс для бинарных операций
    private interface BiOperation {
        double apply(double u, double v);
    }

    // Существующий метод asPoints
    public static Point[] asPoints(TabulatedFunction function) {
        int pointCount = function.getCount();
        Point[] points = new Point[pointCount];

        int i = 0;
        for (Point point : function) {
            points[i] = point;
            i++;
        }

        return points;
    }

    // Приватный метод для выполнения операций
    private TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation) {
        // Проверяем одинаковое количество точек
        if (a.getCount() != b.getCount()) {
            throw new InconsistentFunctionsException("Функции имеют разное количество точек");
        }

        // Получаем точки обеих функций
        Point[] pointsA = asPoints(a);
        Point[] pointsB = asPoints(b);

        int pointCount = pointsA.length;
        double[] xValues = new double[pointCount];
        double[] yValues = new double[pointCount];

        // Обрабатываем каждую точку
        for (int i = 0; i < pointCount; i++) {
            // Проверяем совпадение x-координат
            if (Math.abs(pointsA[i].x - pointsB[i].x) > 1e-10) {
                throw new InconsistentFunctionsException("X-координаты функций не совпадают");
            }

            xValues[i] = pointsA[i].x;
            yValues[i] = operation.apply(pointsA[i].y, pointsB[i].y);
        }

        // Создаем новую функцию через фабрику
        return factory.create(xValues, yValues);
    }

    // Публичные методы операций
    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u + v);
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u - v);
    }
    // МЕТОДЫ УМНОЖЕНИЯ И ДЕЛЕНИЯ
    public TabulatedFunction multiply(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u * v);
    }

    public TabulatedFunction divide(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> {
            if (Math.abs(v) < 1e-12) {
                throw new ArithmeticException("Деление на ноль");
            }
            return u / v;
        });
    }

}
