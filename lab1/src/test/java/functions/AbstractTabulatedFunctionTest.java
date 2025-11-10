package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InterpolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractTabulatedFunctionTest {


    @Test
    public void testCheckLengthIsTheSame_Success() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
    }

    @Test
    public void testCheckLengthIsTheSame_ThrowsException() {
        // Исключение - массивы разной длины
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0}; // Длина 3 vs 2

        assertThrows(DifferentLengthOfArraysException.class, () -> {
            AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        });
    }


    @Test
    public void testCheckSorted_Success() {
        // Успешный случай - массив отсортирован
        double[] sortedArray = {1.0, 2.0, 3.0, 4.0};
        AbstractTabulatedFunction.checkSorted(sortedArray);
        // Если не брошено исключение - тест пройден
    }

    @Test
    public void testCheckSorted_ThrowsException_NotSorted() {
        double[] notSortedArray = {1.0, 3.0, 2.0, 4.0};

        assertThrows(ArrayIsNotSortedException.class, () -> {
            AbstractTabulatedFunction.checkSorted(notSortedArray);
        });
    }

    @Test
    public void testCheckSorted_ThrowsException_EqualValues() {
        double[] arrayWithEqual = {1.0, 2.0, 2.0, 3.0};

        assertThrows(ArrayIsNotSortedException.class, () -> {
            AbstractTabulatedFunction.checkSorted(arrayWithEqual);
        });
    }


    @Test
    public void testArrayFunctionConstructor_LessThan2Points() {
        double[] xValues = {1.0};
        double[] yValues = {2.0};

        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    public void testArrayFunctionConstructor_DifferentLengths() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0};

        assertThrows(DifferentLengthOfArraysException.class, () -> {
            new ArrayTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    public void testArrayFunctionConstructor_UnsortedX() {
        double[] xValues = {3.0, 1.0, 2.0};
        double[] yValues = {4.0, 5.0, 6.0};

        assertThrows(ArrayIsNotSortedException.class, () -> {
            new ArrayTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    public void testLinkedListFunctionConstructor_LessThan2Points() {
        double[] xValues = {1.0};
        double[] yValues = {2.0};

        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    public void testLinkedListFunctionConstructor_DifferentLengths() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0};

        assertThrows(DifferentLengthOfArraysException.class, () -> {
            new LinkedListTabulatedFunction(xValues, yValues);
        });
    }

    @Test
    public void testLinkedListFunctionConstructor_UnsortedX() {
        double[] xValues = {3.0, 1.0, 2.0};
        double[] yValues = {4.0, 5.0, 6.0};

        assertThrows(ArrayIsNotSortedException.class, () -> {
            new LinkedListTabulatedFunction(xValues, yValues);
        });
    }



}