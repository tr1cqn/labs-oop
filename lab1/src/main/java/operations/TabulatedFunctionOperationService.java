package operations;

import functions.Point;
import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;
import functions.factory.ArrayTabulatedFunctionFactory;
import exceptions.InconsistentFunctionsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TabulatedFunctionOperationService {
    private static final Logger logger = LogManager.getLogger(TabulatedFunctionOperationService.class);
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
        logger.debug("Выполнение операции над функциями, тип A: {}, тип B: {}, количество точек A: {}, B: {}", 
            a.getClass().getSimpleName(), b.getClass().getSimpleName(), a.getCount(), b.getCount());
        // Проверяем одинаковое количество точек
        if (a.getCount() != b.getCount()) {
            logger.error("Функции имеют разное количество точек: A={}, B={}", a.getCount(), b.getCount());
            throw new InconsistentFunctionsException("Функции имеют разное количество точек");
        }

        // Получаем точки обеих функций
        Point[] pointsA = asPoints(a);
        Point[] pointsB = asPoints(b);

        int pointCount = pointsA.length;
        logger.debug("Обработка {} точек", pointCount);
        double[] xValues = new double[pointCount];
        double[] yValues = new double[pointCount];

        // Обрабатываем каждую точку
        for (int i = 0; i < pointCount; i++) {
            // Проверяем совпадение x-координат
            if (Math.abs(pointsA[i].x - pointsB[i].x) > 1e-10) {
                logger.error("X-координаты функций не совпадают в точке {}: A.x={}, B.x={}", 
                    i, pointsA[i].x, pointsB[i].x);
                throw new InconsistentFunctionsException("X-координаты функций не совпадают");
            }

            xValues[i] = pointsA[i].x;
            yValues[i] = operation.apply(pointsA[i].y, pointsB[i].y);
            logger.trace("Обработана точка {}: x={}, результат операции: {}", i, xValues[i], yValues[i]);
        }

        // Создаем новую функцию через фабрику
        TabulatedFunction result = factory.create(xValues, yValues);
        logger.info("Операция над функциями завершена успешно, тип результата: {}", 
            result.getClass().getSimpleName());
        return result;
    }

    // Публичные методы операций
    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        logger.info("Сложение функций: {} + {}", a.getClass().getSimpleName(), b.getClass().getSimpleName());
        return doOperation(a, b, (u, v) -> u + v);
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        logger.info("Вычитание функций: {} - {}", a.getClass().getSimpleName(), b.getClass().getSimpleName());
        return doOperation(a, b, (u, v) -> u - v);
    }
    // МЕТОДЫ УМНОЖЕНИЯ И ДЕЛЕНИЯ
    public TabulatedFunction multiply(TabulatedFunction a, TabulatedFunction b) {
        logger.info("Умножение функций: {} * {}", a.getClass().getSimpleName(), b.getClass().getSimpleName());
        return doOperation(a, b, (u, v) -> u * v);
    }

    public TabulatedFunction divide(TabulatedFunction a, TabulatedFunction b) {
        logger.info("Деление функций: {} / {}", a.getClass().getSimpleName(), b.getClass().getSimpleName());
        return doOperation(a, b, (u, v) -> {
            if (Math.abs(v) < 1e-12) {
                logger.error("Попытка деления на ноль: делитель={}", v);
                throw new ArithmeticException("Деление на ноль");
            }
            return u / v;
        });
    }

}
