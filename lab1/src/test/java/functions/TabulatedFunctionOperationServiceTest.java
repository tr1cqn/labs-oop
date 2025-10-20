package functions;

import operations.TabulatedFunctionOperationService;
import functions.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TabulatedFunctionOperationServiceTest {

    @Test
    public void testAsPointsWithArrayFunction() {
        // Создаем функцию на основе массива
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        Point[] points = TabulatedFunctionOperationService.asPoints(function);

        // Проверяем количество точек
        assertEquals(3, points.length);

        // Проверяем значения точек
        assertEquals(1.0, points[0].x, 1e-10);
        assertEquals(10.0, points[0].y, 1e-10);
        assertEquals(2.0, points[1].x, 1e-10);
        assertEquals(20.0, points[1].y, 1e-10);
        assertEquals(3.0, points[2].x, 1e-10);
        assertEquals(30.0, points[2].y, 1e-10);
    }

    @Test
    public void testAsPointsWithLinkedListFunction() {
        // Создаем функцию на основе связного списка
        double[] xValues = {0.5, 1.5, 2.5};
        double[] yValues = {5.0, 15.0, 25.0};

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        Point[] points = TabulatedFunctionOperationService.asPoints(function);

        // Проверяем количество точек
        assertEquals(3, points.length);

        // Проверяем значения точек
        assertEquals(0.5, points[0].x, 1e-10);
        assertEquals(5.0, points[0].y, 1e-10);
        assertEquals(1.5, points[1].x, 1e-10);
        assertEquals(15.0, points[1].y, 1e-10);
        assertEquals(2.5, points[2].x, 1e-10);
        assertEquals(25.0, points[2].y, 1e-10);
    }

    @Test
    public void testAsPointsWithTwoPointFunction() {
        // Тест с функцией из двух точек
        double[] xValues = {1.0, 2.0};
        double[] yValues = {2.0, 3.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        Point[] points = TabulatedFunctionOperationService.asPoints(function);

        assertEquals(2, points.length);
        assertEquals(1.0, points[0].x, 1e-10);
        assertEquals(2.0, points[0].y, 1e-10);
        assertEquals(2.0, points[1].x, 1e-10);
        assertEquals(3.0, points[1].y, 1e-10);
    }
}