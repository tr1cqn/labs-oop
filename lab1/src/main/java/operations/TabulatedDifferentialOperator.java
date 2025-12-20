package operations;

import functions.*;
import functions.factory.*;
import operations.TabulatedFunctionOperationService;
import concurrent.SynchronizedTabulatedFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {
    private static final Logger logger = LogManager.getLogger(TabulatedDifferentialOperator.class);
    private TabulatedFunctionFactory factory;

    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
        logger.debug("Создан TabulatedDifferentialOperator с фабрикой по умолчанию: {}", 
            factory.getClass().getSimpleName());
    }

    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
        logger.debug("Создан TabulatedDifferentialOperator с фабрикой: {}", 
            factory.getClass().getSimpleName());
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        logger.info("Вычисление производной для функции типа: {}, количество точек: {}", 
            function.getClass().getSimpleName(), function.getCount());
        // Получаем все точки функции
        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        int pointCount = points.length;
        logger.debug("Получено {} точек для вычисления производной", pointCount);

        if (pointCount < 2) {
            logger.warn("Недостаточно точек для вычисления производной: {}", pointCount);
        }

        // массивы для x и y значений производной
        double[] xValues = new double[pointCount];
        double[] yValues = new double[pointCount];

        for (int i = 0; i < pointCount; i++) {
            xValues[i] = points[i].x;
        }

        // Вычисляем производную с помощью численного дифференцирования
        for (int i = 1; i < pointCount - 1; i++) {
            double leftX = points[i - 1].x;
            double rightX = points[i + 1].x;
            double leftY = points[i - 1].y;
            double rightY = points[i + 1].y;
            yValues[i] = (rightY - leftY) / (rightX - leftX);
            logger.trace("Вычислена производная в точке {}: f'({})={}", i, points[i].x, yValues[i]);
        }

        if (pointCount > 1) {
            double x0 = points[0].x;
            double x1 = points[1].x;
            double y0 = points[0].y;
            double y1 = points[1].y;
            yValues[0] = (y1 - y0) / (x1 - x0);
            logger.debug("Вычислена производная в начальной точке: f'({})={}", x0, yValues[0]);
        }

        if (pointCount > 1) {
            int last = pointCount - 1;
            double xLast = points[last].x;
            double xPrev = points[last - 1].x;
            double yLast = points[last].y;
            double yPrev = points[last - 1].y;
            yValues[last] = (yLast - yPrev) / (xLast - xPrev);
            logger.debug("Вычислена производная в конечной точке: f'({})={}", xLast, yValues[last]);
        }

        if (pointCount == 1) {
            yValues[0] = 0.0;
            logger.debug("Функция содержит одну точку, производная установлена в 0");
        }

        // Создаем новую табулированную функцию через фабрику
        TabulatedFunction result = factory.create(xValues, yValues);
        logger.info("Производная успешно вычислена, тип результата: {}, количество точек: {}", 
            result.getClass().getSimpleName(), result.getCount());
        return result;
    }
    public TabulatedFunction deriveSynchronously(TabulatedFunction function) {
        logger.info("Вычисление производной синхронно для функции типа: {}", 
            function.getClass().getSimpleName());
        SynchronizedTabulatedFunction syncFunction;
        if (function instanceof SynchronizedTabulatedFunction) {
            logger.debug("Функция уже является SynchronizedTabulatedFunction");
            syncFunction = (SynchronizedTabulatedFunction) function;
        } else {
            logger.debug("Создание SynchronizedTabulatedFunction для синхронного доступа");
            syncFunction = new SynchronizedTabulatedFunction(function);
        }

        TabulatedFunction result = syncFunction.doSynchronously(f -> this.derive(f));
        logger.info("Синхронное вычисление производной завершено");
        return result;
    }
}