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
}