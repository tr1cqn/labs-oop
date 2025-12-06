package concurrent;

import functions.*;
import functions.factory.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
    @Test
    public void testIteratorBasic() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        TabulatedFunction baseFunction = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunction = new SynchronizedTabulatedFunction(baseFunction);

        int count = 0;
        for (Point point : syncFunction) {
            assertNotNull(point);
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void testIteratorOrder() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        TabulatedFunction baseFunction = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunction = new SynchronizedTabulatedFunction(baseFunction);

        Iterator<Point> iterator = syncFunction.iterator();
        assertTrue(iterator.hasNext());

        Point point1 = iterator.next();
        assertEquals(1.0, point1.x, 0.0001);
        assertEquals(10.0, point1.y, 0.0001);

        Point point2 = iterator.next();
        assertEquals(2.0, point2.x, 0.0001);
        assertEquals(20.0, point2.y, 0.0001);

        Point point3 = iterator.next();
        assertEquals(3.0, point3.x, 0.0001);
        assertEquals(30.0, point3.y, 0.0001);

        assertFalse(iterator.hasNext());
    }



    @Test
    public void testIteratorWorksWithCopy() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        TabulatedFunction baseFunction = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunction = new SynchronizedTabulatedFunction(baseFunction);
        Iterator<Point> iterator = syncFunction.iterator();

        baseFunction.setY(1, 999.0);

        iterator.next(); // Первая точка
        Point point2 = iterator.next();
        assertEquals(20.0, point2.y, 0.0001);
    }

    @Test
    public void testMultipleIteratorsIndependent() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0};
        TabulatedFunction baseFunction = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunction = new SynchronizedTabulatedFunction(baseFunction);

        Iterator<Point> iterator1 = syncFunction.iterator();
        Iterator<Point> iterator2 = syncFunction.iterator();

        Point point1FromIter1 = iterator1.next();
        Point point1FromIter2 = iterator2.next();

        assertEquals(point1FromIter1.x, point1FromIter2.x, 0.0001);
        assertEquals(point1FromIter1.y, point1FromIter2.y, 0.0001);

        iterator1.next();
        assertTrue(iterator2.hasNext());
    }

}