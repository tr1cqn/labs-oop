package operations;

import functions.*;
import functions.factory.*;
import exceptions.InconsistentFunctionsException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TabulatedFunctionOperationServiceTest {

    @Test
    public void testAdd_SameTypeFunctions() {
        // Сложение функций одного типа
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {10.0, 20.0, 30.0};
        double[] yValues2 = {5.0, 15.0, 25.0};

        TabulatedFunction function1 = new ArrayTabulatedFunction(xValues1, yValues1);
        TabulatedFunction function2 = new ArrayTabulatedFunction(xValues1, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunction result = service.add(function1, function2);

        assertEquals(3, result.getCount());
        assertEquals(15.0, result.getY(0), 1e-10);
        assertEquals(35.0, result.getY(1), 1e-10);
        assertEquals(55.0, result.getY(2), 1e-10);
    }

    @Test
    public void testAdd_DifferentTypeFunctions() {
        // Сложение функций разного типа
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {10.0, 20.0, 30.0};
        double[] yValues2 = {5.0, 15.0, 25.0};

        TabulatedFunction function1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction function2 = new LinkedListTabulatedFunction(xValues, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunction result = service.add(function1, function2);

        assertEquals(3, result.getCount());
        assertEquals(15.0, result.getY(0), 1e-10);
        assertEquals(35.0, result.getY(1), 1e-10);
        assertEquals(55.0, result.getY(2), 1e-10);
    }

    @Test
    public void testSubtract_DifferentTypeFunctions() {
        // Вычитание функций разного типа
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {10.0, 20.0, 30.0};
        double[] yValues2 = {5.0, 15.0, 25.0};

        TabulatedFunction function1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction function2 = new LinkedListTabulatedFunction(xValues, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunction result = service.subtract(function1, function2);

        assertEquals(3, result.getCount());
        assertEquals(5.0, result.getY(0), 1e-10);
        assertEquals(5.0, result.getY(1), 1e-10);
        assertEquals(5.0, result.getY(2), 1e-10);
    }

    @Test
    public void testAdd_ThrowsException_DifferentPointCount() {
        // Исключение при разном количестве точек
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {10.0, 20.0, 30.0};
        double[] xValues2 = {1.0, 2.0};
        double[] yValues2 = {5.0, 15.0};

        TabulatedFunction function1 = new ArrayTabulatedFunction(xValues1, yValues1);
        TabulatedFunction function2 = new ArrayTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        assertThrows(InconsistentFunctionsException.class, () -> {
            service.add(function1, function2);
        });
    }

    @Test
    public void testAdd_ThrowsException_DifferentXValues() {
        // Исключение при разных x-координатах
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {10.0, 20.0, 30.0};
        double[] xValues2 = {1.0, 2.5, 3.0};
        double[] yValues2 = {5.0, 15.0, 25.0};

        TabulatedFunction function1 = new ArrayTabulatedFunction(xValues1, yValues1);
        TabulatedFunction function2 = new ArrayTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        assertThrows(InconsistentFunctionsException.class, () -> {
            service.add(function1, function2);
        });
    }

    @Test
    public void testDifferentFactories() {
        // Тест с разными фабриками
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {10.0, 20.0, 30.0};
        double[] yValues2 = {5.0, 15.0, 25.0};

        TabulatedFunction function1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction function2 = new LinkedListTabulatedFunction(xValues, yValues2);

        // Тест с фабрикой для связного списка
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(
                new LinkedListTabulatedFunctionFactory()
        );

        TabulatedFunction result = service.add(function1, function2);
        assertTrue(result instanceof LinkedListTabulatedFunction);
    }
    @Test
    void testMultiplyCompatibleFunctions() {
        // Тест умножения совместимых функций
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {2.0, 4.0, 6.0};
        double[] yValues2 = {3.0, 5.0, 7.0};

        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        TabulatedFunction result = service.multiply(func1, func2);

        assertEquals(3, result.getCount());
        assertEquals(6.0, result.getY(0), 1e-10);  // 2 * 3 = 6
        assertEquals(20.0, result.getY(1), 1e-10); // 4 * 5 = 20
        assertEquals(42.0, result.getY(2), 1e-10); // 6 * 7 = 42
    }

    @Test
    void testMultiplyWithLinkedListFunctions() {
        // Тест умножения с LinkedList функциями
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(
                new LinkedListTabulatedFunctionFactory()
        );

        double[] xValues = {-1.0, 0.0, 1.0};
        double[] yValues1 = {2.0, 3.0, 4.0};
        double[] yValues2 = {-1.0, 0.5, 2.0};

        TabulatedFunction func1 = new LinkedListTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new LinkedListTabulatedFunction(xValues, yValues2);

        TabulatedFunction result = service.multiply(func1, func2);

        assertEquals(3, result.getCount());
        assertEquals(-2.0, result.getY(0), 1e-10); // 2 * (-1) = -2
        assertEquals(1.5, result.getY(1), 1e-10);  // 3 * 0.5 = 1.5
        assertEquals(8.0, result.getY(2), 1e-10);  // 4 * 2 = 8
    }

    @Test
    void testDivideCompatibleFunctions() {
        // Тест деления совместимых функций
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        double[] xValues = {1.0, 2.0, 4.0};
        double[] yValues1 = {6.0, 12.0, 24.0};
        double[] yValues2 = {2.0, 3.0, 6.0};

        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        TabulatedFunction result = service.divide(func1, func2);

        assertEquals(3, result.getCount());
        assertEquals(3.0, result.getY(0), 1e-10);  // 6 / 2 = 3
        assertEquals(4.0, result.getY(1), 1e-10);  // 12 / 3 = 4
        assertEquals(4.0, result.getY(2), 1e-10);  // 24 / 6 = 4
    }

    @Test
    void testDivideWithFractions() {
        // Тест деления с дробными результатами
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        double[] xValues = {1.0, 2.0, 4.0};
        double[] yValues1 = {1.0, 1.0, 1.0};
        double[] yValues2 = {2.0, 4.0, 8.0};

        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        TabulatedFunction result = service.divide(func1, func2);

        assertEquals(3, result.getCount());
        assertEquals(0.5, result.getY(0), 1e-10);  // 1 / 2 = 0.5
        assertEquals(0.25, result.getY(1), 1e-10); // 1 / 4 = 0.25
        assertEquals(0.125, result.getY(2), 1e-10); // 1 / 8 = 0.125
    }

    @Test
    void testDivideByZeroThrowsException() {
        // Тест деления на ноль
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 0.0, 1.0};

        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues, yValues1);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues, yValues2);

        assertThrows(ArithmeticException.class, () -> {
            service.divide(func1, func2);
        });
    }

    @Test
    void testMultiplyIncompatibleFunctionsThrowsException() {
        // Тест умножения несовместимых функций
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] xValues2 = {1.0, 2.5, 3.0};
        double[] yValues = {1.0, 2.0, 3.0};

        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues);

        assertThrows(InconsistentFunctionsException.class, () -> {
            service.multiply(func1, func2);
        });
    }

    @Test
    void testDivideIncompatibleFunctionsThrowsException() {
        // Тест деления несовместимых функций
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] xValues2 = {1.0, 2.0, 4.0};
        double[] yValues = {1.0, 2.0, 3.0};

        TabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues);
        TabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues);

        assertThrows(InconsistentFunctionsException.class, () -> {
            service.divide(func1, func2);
        });
    }



}