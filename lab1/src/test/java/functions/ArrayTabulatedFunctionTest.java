package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArrayTabulatedFunctionInsertableTest {

    @Test
    void testInsertAtBeginning() {
        // Вставка точки с x меньше всех существующих
        double[] xValues = {2.0, 3.0, 4.0};
        double[] yValues = {20.0, 30.0, 40.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        func.insert(1.0, 10.0);

        assertEquals(4, func.getCount());
        assertEquals(1.0, func.getX(0), 1e-10);
        assertEquals(2.0, func.getX(1), 1e-10);
        assertEquals(3.0, func.getX(2), 1e-10);
        assertEquals(4.0, func.getX(3), 1e-10);
        assertEquals(10.0, func.getY(0), 1e-10);
    }

    @Test
    void testInsertAtEnd() {
        // Вставка точки с x больше всех существующих
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        func.insert(4.0, 40.0);

        assertEquals(4, func.getCount());
        assertEquals(1.0, func.getX(0), 1e-10);
        assertEquals(2.0, func.getX(1), 1e-10);
        assertEquals(3.0, func.getX(2), 1e-10);
        assertEquals(4.0, func.getX(3), 1e-10);
        assertEquals(40.0, func.getY(3), 1e-10);
    }

    @Test
    void testInsertInMiddle() {
        // Вставка точки между двумя существующими значениями
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {10.0, 30.0, 50.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        func.insert(2.0, 20.0);

        assertEquals(4, func.getCount());
        assertEquals(1.0, func.getX(0), 1e-10);
        assertEquals(2.0, func.getX(1), 1e-10);
        assertEquals(3.0, func.getX(2), 1e-10);
        assertEquals(5.0, func.getX(3), 1e-10);
        assertEquals(20.0, func.getY(1), 1e-10);
    }

    @Test
    void testInsertExistingX() {
        // Вставка точки с существующим x
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        func.insert(2.0, 25.0);

        assertEquals(3, func.getCount());
        assertEquals(2.0, func.getX(1), 1e-10);
        assertEquals(25.0, func.getY(1), 1e-10);
        assertEquals(10.0, func.getY(0), 1e-10);
        assertEquals(30.0, func.getY(2), 1e-10);
    }

    @Test
    void testInsertMultipleElements() {
        // Множественная вставка точек в разном порядке
        double[] xValues = {1.0, 5.0};
        double[] yValues = {10.0, 50.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        func.insert(3.0, 30.0);
        func.insert(2.0, 20.0);
        func.insert(4.0, 40.0);

        assertEquals(5, func.getCount());
        assertEquals(1.0, func.getX(0), 1e-10);
        assertEquals(2.0, func.getX(1), 1e-10);
        assertEquals(3.0, func.getX(2), 1e-10);
        assertEquals(4.0, func.getX(3), 1e-10);
        assertEquals(5.0, func.getX(4), 1e-10);
        assertEquals(20.0, func.getY(1), 1e-10);
        assertEquals(30.0, func.getY(2), 1e-10);
        assertEquals(40.0, func.getY(3), 1e-10);
    }

    @Test
    void testInsertMaintainsOrder() {
        // Проверка сохранения упорядоченности после вставки
        double[] xValues = {1.0, 4.0};
        double[] yValues = {10.0, 40.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        func.insert(3.0, 30.0);
        func.insert(2.0, 20.0);
        func.insert(5.0, 50.0);

        for (int i = 0; i < func.getCount() - 1; i++) {
            assertTrue(func.getX(i) < func.getX(i + 1));
        }
    }

    @Test
    void testInsertAndFunctionality() {
        // Проверка работы функции после вставки
        double[] xValues = {1.0, 3.0};
        double[] yValues = {10.0, 30.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        func.insert(2.0, 20.0);

        assertEquals(3, func.getCount());
        assertEquals(20.0, func.apply(2.0), 1e-10);
        assertEquals(15.0, func.apply(1.5), 1e-10);
        assertEquals(10.0, func.apply(1.0), 1e-10);
        assertEquals(30.0, func.apply(3.0), 1e-10);
    }

    @Test
    void testInsertIntoSmallFunction() {
        // Вставка в функцию с минимальным количеством точек
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        func.insert(0.5, 5.0);
        func.insert(2.5, 25.0);
        func.insert(1.5, 15.0);

        assertEquals(5, func.getCount());
        assertEquals(0.5, func.leftBound(), 1e-10);
        assertEquals(2.5, func.rightBound(), 1e-10);
        assertEquals(0.5, func.getX(0), 1e-10);
        assertEquals(1.0, func.getX(1), 1e-10);
        assertEquals(1.5, func.getX(2), 1e-10);
        assertEquals(2.0, func.getX(3), 1e-10);
        assertEquals(2.5, func.getX(4), 1e-10);
    }

    @Test
    void testInsertWithDuplicateX() {
        // Многократная вставка точки с одинаковым x
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        func.insert(2.0, 25.0);
        func.insert(2.0, 30.0);
        func.insert(2.0, 35.0);

        assertEquals(3, func.getCount());
        assertEquals(35.0, func.getY(1), 1e-10);
        assertEquals(10.0, func.getY(0), 1e-10);
        assertEquals(30.0, func.getY(2), 1e-10);
    }

    @Test
    void testInsertAndBoundsUpdate() {
        // Проверка обновления границ функции после вставки
        double[] xValues = {2.0, 3.0};
        double[] yValues = {20.0, 30.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        double originalLeft = func.leftBound();
        double originalRight = func.rightBound();

        func.insert(1.0, 10.0);
        assertTrue(func.leftBound() < originalLeft);

        func.insert(4.0, 40.0);
        assertTrue(func.rightBound() > originalRight);

        assertEquals(1.0, func.leftBound(), 1e-10);
        assertEquals(4.0, func.rightBound(), 1e-10);
    }

    @Test
    void testInsertWithPrecision() {
        // Проверка работы с числами с плавающей точкой
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 2.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        func.insert(1.5, 1.5);
        func.insert(1.25, 1.25);
        func.insert(1.75, 1.75);

        assertEquals(5, func.getCount());
        assertEquals(1.0, func.getX(0), 1e-10);
        assertEquals(1.25, func.getX(1), 1e-10);
        assertEquals(1.5, func.getX(2), 1e-10);
        assertEquals(1.75, func.getX(3), 1e-10);
        assertEquals(2.0, func.getX(4), 1e-10);
    }
}