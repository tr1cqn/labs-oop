package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionTest {

    @Test
    void testConstructorFromArrays() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};

        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(4, func.getCount());
        assertEquals(1.0, func.getX(0), 1e-10);
        assertEquals(20.0, func.getY(1), 1e-10);
        assertEquals(1.0, func.leftBound(), 1e-10);
        assertEquals(4.0, func.rightBound(), 1e-10);
    }

    @Test
    void testConstructorFromFunction() {
        MathFunction source = new IdentityFunction();
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(source, 0.0, 5.0, 6);

        assertEquals(6, func.getCount());
        assertEquals(0.0, func.leftBound(), 1e-10);
        assertEquals(5.0, func.rightBound(), 1e-10);
        assertEquals(2.0, func.getY(2), 1e-10);
    }

    @Test
    void testConstructorWithSinglePoint() {
        MathFunction source = new IdentityFunction();
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(source, 2.0, 2.0, 3);

        assertEquals(3, func.getCount());
        assertEquals(2.0, func.getX(0), 1e-10);
        assertEquals(2.0, func.getX(1), 1e-10);
        assertEquals(2.0, func.getX(2), 1e-10);
        assertEquals(2.0, func.getY(0), 1e-10);
    }

    @Test
    void testGetSetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        func.setY(1, 25.0);
        assertEquals(25.0, func.getY(1), 1e-10);
    }

    @Test
    void testIndexOf() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(1, func.indexOfX(2.0));
        assertEquals(-1, func.indexOfX(5.0));
        assertEquals(2, func.indexOfY(30.0));
        assertEquals(-1, func.indexOfY(50.0));
    }

    @Test
    void testFloorIndexOfX() {
        double[] xValues = {1.0, 2.0, 4.0, 5.0};
        double[] yValues = {10.0, 20.0, 40.0, 50.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(0, func.floorIndexOfX(0.5)); // меньше всех
        assertEquals(1, func.floorIndexOfX(2.0)); // точное совпадение
        assertEquals(1, func.floorIndexOfX(3.0)); // между 2 и 4
        assertEquals(2, func.floorIndexOfX(4.5)); // между 4 и 5
        assertEquals(4, func.floorIndexOfX(6.0)); // больше всех
    }

    @Test
    void testApply() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        // Точное значение
        assertEquals(20.0, func.apply(2.0), 1e-10);

        // Интерполяция
        assertEquals(15.0, func.apply(1.5), 1e-10);

        // Экстраполяция слева
        double leftResult = func.apply(0.5);
        assertTrue(leftResult < 10.0);

        // Экстраполяция справа
        double rightResult = func.apply(3.5);
        assertTrue(rightResult > 30.0);
    }

    @Test
    void testSinglePointFunction() {
        double[] xValues = {2.0};
        double[] yValues = {5.0};

        // Должна быть ошибка - нужно минимум 2 точки
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    void testInvalidArrays() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0}; // разная длина

        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(xValues, yValues);
        });
    }
}