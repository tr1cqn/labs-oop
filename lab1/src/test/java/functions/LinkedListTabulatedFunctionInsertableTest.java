package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionInsertableTest {

    @Test
    void testInsertReplaceExisting() {
        double[] xs = {1.0, 2.0, 3.0};
        double[] ys = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xs, ys);

        func.insert(2.0, 200.0); // Заменяем существующее значение

        assertEquals(3, func.getCount());
        assertEquals(200.0, func.getY(1));
        assertEquals(200.0, func.apply(2.0));
    }

    @Test
    void testInsertInMiddle() {
        double[] xs = {1.0, 3.0};
        double[] ys = {10.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xs, ys);

        func.insert(2.0, 20.0); // Вставляем между 1.0 и 3.0

        assertEquals(3, func.getCount());
        assertEquals(1.0, func.getX(0));
        assertEquals(2.0, func.getX(1));
        assertEquals(3.0, func.getX(2));
        assertEquals(20.0, func.getY(1));
    }

    @Test
    void testInsertAtBeginning() {
        double[] xs = {2.0, 3.0};
        double[] ys = {20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xs, ys);

        func.insert(1.0, 10.0); // Вставляем в начало

        assertEquals(3, func.getCount());
        assertEquals(1.0, func.getX(0));
        assertEquals(2.0, func.getX(1));
        assertEquals(3.0, func.getX(2));
        assertEquals(10.0, func.getY(0));
    }

    @Test
    void testInsertAtEnd() {
        double[] xs = {1.0, 2.0};
        double[] ys = {10.0, 20.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xs, ys);

        func.insert(3.0, 30.0); // Вставляем в конец

        assertEquals(3, func.getCount());
        assertEquals(1.0, func.getX(0));
        assertEquals(2.0, func.getX(1));
        assertEquals(3.0, func.getX(2));
        assertEquals(30.0, func.getY(2));
    }

}

