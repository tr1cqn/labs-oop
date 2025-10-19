package functions.factory;

import functions.TabulatedFunction;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TabulatedFunctionFactoryTest {

    @Test
    void testArrayTabulatedFunctionFactory() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        assertTrue(function instanceof ArrayTabulatedFunction);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(30.0, function.getY(2), 1e-10);
    }

    @Test
    void testLinkedListTabulatedFunctionFactory() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        assertTrue(function instanceof LinkedListTabulatedFunction);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(30.0, function.getY(2), 1e-10);
    }

    @Test
    void testFactoryCreatesFunctionalObjects() {
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();

        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};

        TabulatedFunction arrayFunc = arrayFactory.create(xValues, yValues);
        TabulatedFunction linkedListFunc = linkedListFactory.create(xValues, yValues);

        // Проверяем, что созданные объекты работают корректно
        assertEquals(1.0, arrayFunc.apply(1.0), 1e-10);
        assertEquals(4.0, arrayFunc.apply(2.0), 1e-10);
        assertEquals(1.0, linkedListFunc.apply(1.0), 1e-10);
        assertEquals(4.0, linkedListFunc.apply(2.0), 1e-10);
    }

    @Test
    void testFactoryWithMinimumPoints() {
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();

        double[] xValues = {1.0, 2.0}; // минимальное количество точек
        double[] yValues = {10.0, 20.0};

        TabulatedFunction arrayFunc = arrayFactory.create(xValues, yValues);
        TabulatedFunction linkedListFunc = linkedListFactory.create(xValues, yValues);

        assertTrue(arrayFunc instanceof ArrayTabulatedFunction);
        assertTrue(linkedListFunc instanceof LinkedListTabulatedFunction);
        assertEquals(2, arrayFunc.getCount());
        assertEquals(2, linkedListFunc.getCount());
    }

}