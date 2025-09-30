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
    void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(1, func.indexOfX(2.0));
        assertEquals(-1, func.indexOfX(5.0));
    }

    @Test
    void testIndexOfY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(2, func.indexOfY(30.0));
        assertEquals(-1, func.indexOfY(50.0));
    }

    @Test
    void testBounds() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(1.0, func.leftBound(), 1e-10);
        assertEquals(3.0, func.rightBound(), 1e-10);
    }

    @Test
    void testFloorIndexOfX() {
        double[] xValues = {1.0, 2.0, 4.0, 5.0};
        double[] yValues = {10.0, 20.0, 40.0, 50.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(0, func.floorIndexOfX(0.5));
        assertEquals(1, func.floorIndexOfX(2.0));
        assertEquals(1, func.floorIndexOfX(3.0));
        assertEquals(2, func.floorIndexOfX(4.5));
        assertEquals(4, func.floorIndexOfX(6.0));
    }

    @Test
    void testApplyExact() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(20.0, func.apply(2.0), 1e-10);
    }

    @Test
    void testApplyInterpolation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(15.0, func.apply(1.5), 1e-10);
    }

    @Test
    void testApplyExtrapolationLeft() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        double result = func.apply(0.5);
        assertTrue(result < 10.0);
    }

    @Test
    void testApplyExtrapolationRight() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        double result = func.apply(3.5);
        assertTrue(result > 30.0);
    }

    @Test
    void testExtrapolateLeft() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        double result = func.extrapolateLeft(0.0);
        assertEquals(0.0, result, 1e-10);
    }

    @Test
    void testExtrapolateRight() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        double result = func.extrapolateRight(4.0);
        assertEquals(40.0, result, 1e-10);
    }

    @Test
    void testInterpolateWithIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        double result = func.interpolate(1.5, 0);
        assertEquals(15.0, result, 1e-10);
    }

    @Test
    void testInvalidIndices() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        assertThrows(IndexOutOfBoundsException.class, () -> func.getX(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> func.getX(5));
    }

    @Test
    void testInvalidArrays() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0};

        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    void testUnorderedX() {
        double[] xValues = {1.0, 3.0, 2.0};
        double[] yValues = {10.0, 30.0, 20.0};

        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    void testSinglePointError() {
        double[] xValues = {2.0};
        double[] yValues = {5.0};

        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    void testEmptyFunctionBounds() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(new IdentityFunction(), 1.0, 2.0, 2);

        assertDoesNotThrow(() -> {
            func.leftBound();
            func.rightBound();
        });
    }

    @Test
    void testInterpolateMethod() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

        double result = func.interpolate(1.5, 1.0, 2.0, 10.0, 20.0);
        assertEquals(15.0, result, 1e-10);
    }
}