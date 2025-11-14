package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AbstractTabulatedFunctionToStringTest {

    @Test
    void testArrayTabulatedFunctionToString() {
        // Тест строкового представления ArrayTabulatedFunction
        double[] xValues = {0.0, 0.5, 1.0};
        double[] yValues = {0.0, 0.25, 1.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "ArrayTabulatedFunction size = 3\n" +
                "[0.0; 0.0]\n" +
                "[0.5; 0.25]\n" +
                "[1.0; 1.0]\n";

        assertEquals(expected, result);
    }

    @Test
    void testLinkedListTabulatedFunctionToString() {
        // Тест строкового представления LinkedListTabulatedFunction
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "LinkedListTabulatedFunction size = 3\n" +
                "[1.0; 10.0]\n" +
                "[2.0; 20.0]\n" +
                "[3.0; 30.0]\n";

        assertEquals(expected, result);
    }

    @Test
    void testToStringWithTwoPoints() {
        // Тест с минимальным количеством точек
        double[] xValues = {-1.0, 1.0};
        double[] yValues = {1.0, 1.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "ArrayTabulatedFunction size = 2\n" +
                "[-1.0; 1.0]\n" +
                "[1.0; 1.0]\n";

        assertEquals(expected, result);
    }

    @Test
    void testToStringWithMultiplePoints() {
        // Тест с большим количеством точек
        double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0};

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "LinkedListTabulatedFunction size = 5\n" +
                "[0.0; 0.0]\n" +
                "[1.0; 1.0]\n" +
                "[2.0; 4.0]\n" +
                "[3.0; 9.0]\n" +
                "[4.0; 16.0]\n";

        assertEquals(expected, result);
    }

    @Test
    void testToStringWithNegativeValues() {
        // Тест с отрицательными значениями
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues = {4.0, 1.0, 0.0, 1.0, 4.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "ArrayTabulatedFunction size = 5\n" +
                "[-2.0; 4.0]\n" +
                "[-1.0; 1.0]\n" +
                "[0.0; 0.0]\n" +
                "[1.0; 1.0]\n" +
                "[2.0; 4.0]\n";

        assertEquals(expected, result);
    }

    @Test
    void testToStringWithDecimalValues() {
        // Тест с дробными значениями
        double[] xValues = {0.1, 0.2, 0.3};
        double[] yValues = {0.01, 0.04, 0.09};

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "LinkedListTabulatedFunction size = 3\n" +
                "[0.1; 0.01]\n" +
                "[0.2; 0.04]\n" +
                "[0.3; 0.09]\n";

        assertEquals(expected, result);
    }

    @Test
    void testToStringFormatConsistency() {
        double[] xValues = {1.5, 2.5, 3.5};
        double[] yValues = {2.25, 6.25, 12.25};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        String result = function.toString();


        assertTrue(result.contains("ArrayTabulatedFunction size = 3"));
        assertTrue(result.contains("[1.5; 2.25]"));
        assertTrue(result.contains("[2.5; 6.25]"));
        assertTrue(result.contains("[3.5; 12.25]"));
        assertTrue(result.startsWith("ArrayTabulatedFunction"));
        assertTrue(result.endsWith("12.25]\n"));

        // Проверяем количество переносов строк
        long lineCount = result.chars().filter(ch -> ch == '\n').count();
        assertEquals(4, lineCount); // 1 заголовок + 3 точки
    }
}