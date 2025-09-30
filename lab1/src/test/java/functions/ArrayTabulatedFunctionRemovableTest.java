package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArrayTabulatedFunctionRemovableTest {

    @Test
    void testRemoveMiddle() {
        double[] xs = {1.0, 2.0, 3.0, 4.0};
        double[] ys = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xs, ys);

        func.remove(1); // Удаляем элемент с индексом 1 (x=2.0)

        assertEquals(3, func.getCount());
        assertEquals(1.0, func.getX(0));
        assertEquals(3.0, func.getX(1));
        assertEquals(4.0, func.getX(2));
        assertEquals(30.0, func.getY(1));
    }

    @Test
    void testRemoveFirst() {
        double[] xs = {1.0, 2.0, 3.0};
        double[] ys = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xs, ys);

        func.remove(0); // Удаляем первый элемент

        assertEquals(2, func.getCount());
        assertEquals(2.0, func.getX(0));
        assertEquals(3.0, func.getX(1));
        assertEquals(20.0, func.getY(0));
    }

    @Test
    void testRemoveLast() {
        double[] xs = {1.0, 2.0, 3.0};
        double[] ys = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xs, ys);

        func.remove(2); // Удаляем последний элемент

        assertEquals(2, func.getCount());
        assertEquals(1.0, func.getX(0));
        assertEquals(2.0, func.getX(1));
        assertEquals(10.0, func.getY(0));
        assertEquals(20.0, func.getY(1));
    }

    @Test
    void testRemoveInvalidIndex() {
        double[] xs = {1.0, 2.0};
        double[] ys = {10.0, 20.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xs, ys);

        // Проверяем исключения для неверных индексов
        assertThrows(IndexOutOfBoundsException.class, () -> func.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> func.remove(2));
    }
}
