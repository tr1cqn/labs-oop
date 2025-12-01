package concurrent;

import functions.*;
import functions.factory.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SynchronizedTabulatedFunctionTest {

    @Test
    void testGetCount() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction baseFunction = factory.create(new double[]{1, 2, 3}, new double[]{4, 5, 6});
        SynchronizedTabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(baseFunction);

        assertEquals(3, synchronizedFunction.getCount());
    }

    @Test
    void testGetX() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunction baseFunction = factory.create(new double[]{1, 2, 3}, new double[]{4, 5, 6});
        SynchronizedTabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(baseFunction);

        assertEquals(1.0, synchronizedFunction.getX(0));
        assertEquals(2.0, synchronizedFunction.getX(1));
        assertEquals(3.0, synchronizedFunction.getX(2));
    }

    @Test
    void testGetY() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction baseFunction = factory.create(new double[]{1, 2, 3}, new double[]{4, 5, 6});
        SynchronizedTabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(baseFunction);

        assertEquals(4.0, synchronizedFunction.getY(0));
        assertEquals(5.0, synchronizedFunction.getY(1));
        assertEquals(6.0, synchronizedFunction.getY(2));
    }

    @Test
    void testSetY() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunction baseFunction = factory.create(new double[]{1, 2, 3}, new double[]{4, 5, 6});
        SynchronizedTabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(baseFunction);

        synchronizedFunction.setY(1, 10.0);
        assertEquals(10.0, synchronizedFunction.getY(1));
    }

    @Test
    void testIndexOfX() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction baseFunction = factory.create(new double[]{1, 2, 3}, new double[]{4, 5, 6});
        SynchronizedTabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(baseFunction);

        assertEquals(1, synchronizedFunction.indexOfX(2));
        assertEquals(-1, synchronizedFunction.indexOfX(5));
    }

    @Test
    void testIndexOfY() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunction baseFunction = factory.create(new double[]{1, 2, 3}, new double[]{4, 5, 6});
        SynchronizedTabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(baseFunction);

        assertEquals(0, synchronizedFunction.indexOfY(4));
        assertEquals(-1, synchronizedFunction.indexOfY(10));
    }

    @Test
    void testLeftBound() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction baseFunction = factory.create(new double[]{1, 2, 3}, new double[]{4, 5, 6});
        SynchronizedTabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(baseFunction);

        assertEquals(1.0, synchronizedFunction.leftBound());
    }

    @Test
    void testRightBound() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunction baseFunction = factory.create(new double[]{1, 2, 3}, new double[]{4, 5, 6});
        SynchronizedTabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(baseFunction);

        assertEquals(3.0, synchronizedFunction.rightBound());
    }

    @Test
    void testApply() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction baseFunction = factory.create(new double[]{1, 2, 3}, new double[]{1, 4, 9});
        SynchronizedTabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(baseFunction);

        assertEquals(1.0, synchronizedFunction.apply(1));
        assertEquals(4.0, synchronizedFunction.apply(2));
        assertEquals(9.0, synchronizedFunction.apply(3));
    }

    @Test
    void testIterator() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunction baseFunction = factory.create(new double[]{1, 2, 3}, new double[]{4, 5, 6});
        SynchronizedTabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(baseFunction);

        int count = 0;
        for (Point point : synchronizedFunction) {
            count++;
            assertNotNull(point);
        }
        assertEquals(3, count);
    }
}