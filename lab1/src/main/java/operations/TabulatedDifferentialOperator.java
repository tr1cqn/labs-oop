package operations;

import functions.*;
import functions.factory.*;
import operations.TabulatedFunctionOperationService;
import concurrent.SynchronizedTabulatedFunction;

public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {
    private TabulatedFunctionFactory factory;

    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        // Получаем все точки функции
        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        int pointCount = points.length;

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
        }


        if (pointCount > 1) {
            double x0 = points[0].x;
            double x1 = points[1].x;
            double y0 = points[0].y;
            double y1 = points[1].y;
            yValues[0] = (y1 - y0) / (x1 - x0);
        }

        if (pointCount > 1) {
            int last = pointCount - 1;
            double xLast = points[last].x;
            double xPrev = points[last - 1].x;
            double yLast = points[last].y;
            double yPrev = points[last - 1].y;
            yValues[last] = (yLast - yPrev) / (xLast - xPrev);
        }

        if (pointCount == 1) {
            yValues[0] = 0.0;
        }

        // Создаем новую табулированную функцию через фабрику
        return factory.create(xValues, yValues);
    }
    public TabulatedFunction deriveSynchronously(TabulatedFunction function) {
        SynchronizedTabulatedFunction syncFunction;
        if (function instanceof SynchronizedTabulatedFunction) {
            syncFunction = (SynchronizedTabulatedFunction) function;
        } else {
            syncFunction = new SynchronizedTabulatedFunction(function);
        }

        return syncFunction.doSynchronously(f -> this.derive(f));
    }
}