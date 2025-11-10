package operations;

import functions.*;
import functions.factory.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class TabulatedDifferentialOperatorTest {

    @Test
    void testDeriveLinearFunctionWithArrayFactory() {
        // Тест производной функции f(x) = 2x + 1 с Array фабрикой
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {1.0, 3.0, 5.0, 7.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(new ArrayTabulatedFunctionFactory());

        TabulatedFunction derivative = operator.derive(function);

        assertEquals(4, derivative.getCount());
        // Проверяем, что производная постоянна и равна 2
        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(2.0, derivative.getY(i), 1e-10);
        }
    }

    @Test
    void testDeriveQuadraticFunctionWithLinkedListFactory() {
        // Тест производной  функции f(x) = x² с LinkedList фабрикой
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction derivative = operator.derive(function);

        // Проверяем количество точек
        assertEquals(4, derivative.getCount());
        assertEquals(1.0, derivative.getY(0), 1e-1);  // f'(0) ≈ 1.0
        assertEquals(2.0, derivative.getY(1), 1e-1);  // f'(1) ≈ 2.0
        assertEquals(4.0, derivative.getY(2), 1e-1);  // f'(2) ≈ 4.0
        assertEquals(5.0, derivative.getY(3), 1e-1);  // f'(3) ≈ 5.0
    }

    @Test
    void testDeriveWithDefaultConstructor() {
        // Тест конструктора по умолчанию (должен использовать Array фабрику)
        double[] xValues = {0.0, 1.0};
        double[] yValues = {0.0, 1.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        TabulatedFunction derivative = operator.derive(function);

        // Проверяем тип созданной функции
        assertTrue(derivative instanceof ArrayTabulatedFunction);

        assertEquals(2, derivative.getCount());
        assertEquals(1.0, derivative.getY(0), 1e-10);
        assertEquals(1.0, derivative.getY(1), 1e-10);
    }

    @Test
    void testDeriveTwoPointFunction() {
        // Тест функции с минимальным количеством точек
        double[] xValues = {1.0, 2.0};
        double[] yValues = {3.0, 7.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        TabulatedFunction derivative = operator.derive(function);

        // Проверяем количество точек
        assertEquals(2, derivative.getCount());
        // Проверяем значения производной
        assertEquals(4.0, derivative.getY(0), 1e-10);
        assertEquals(4.0, derivative.getY(1), 1e-10);
    }

    @Test
    void testFactoryGetterAndSetter() {
        // Тест геттера и сеттера фабрики
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        // Проверяем фабрику по умолчанию
        assertTrue(operator.getFactory() instanceof ArrayTabulatedFunctionFactory);

        // Устанавливаем новую фабрику
        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();
        operator.setFactory(newFactory);
        assertEquals(newFactory, operator.getFactory());

        // Проверяем, что производная создается через новую фабрику
        double[] xValues = {0.0, 1.0};
        double[] yValues = {0.0, 1.0};
        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        TabulatedFunction derivative = operator.derive(function);
        assertTrue(derivative instanceof LinkedListTabulatedFunction);
    }

    @Test
    void testDeriveWithDifferentFactoryTypes() {
        // Тест работы с разными типами фабрик
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0}; // f(x) = x²

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        //  Array фабрик
        TabulatedDifferentialOperator arrayOperator = new TabulatedDifferentialOperator(new ArrayTabulatedFunctionFactory());
        TabulatedFunction arrayDerivative = arrayOperator.derive(function);
        assertTrue(arrayDerivative instanceof ArrayTabulatedFunction);

        // LinkedList фабрик
        TabulatedDifferentialOperator linkedListOperator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());
        TabulatedFunction linkedListDerivative = linkedListOperator.derive(function);
        assertTrue(linkedListDerivative instanceof LinkedListTabulatedFunction);

        // Проверяем, что значения производной одинаковы для обоих типов фабрик
        assertEquals(arrayDerivative.getCount(), linkedListDerivative.getCount());
        for (int i = 0; i < arrayDerivative.getCount(); i++) {
            assertEquals(arrayDerivative.getX(i), linkedListDerivative.getX(i), 1e-10);
            assertEquals(arrayDerivative.getY(i), linkedListDerivative.getY(i), 1e-10);
        }
    }

    @Test
    void testDeriveConstantFunction() {
        // Тест производной константной функции f(x) = 5
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues = {5.0, 5.0, 5.0, 5.0, 5.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        TabulatedFunction derivative = operator.derive(function);

        // Проверяем количество точек
        assertEquals(5, derivative.getCount());
        // Проверяем, что производная константы равна 0
        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(0.0, derivative.getY(i), 1e-10);
        }
    }

    @Test
    void testDeriveThreePointFunction() {
        // Тест функции с тремя точками
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 2.0, 4.0}; // f(x) = 2x

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        TabulatedFunction derivative = operator.derive(function);

        // Проверяем количество точек
        assertEquals(3, derivative.getCount());
        // Проверяем значения производной
        assertEquals(2.0, derivative.getY(0), 1e-10);
        assertEquals(2.0, derivative.getY(1), 1e-10);
        assertEquals(2.0, derivative.getY(2), 1e-10);
    }

    @Test
    void testDeriveWithNonUniformGrid() {
        double[] xValues = {0.0, 0.5, 1.5, 2.0};
        double[] yValues = {0.0, 0.25, 2.25, 4.0}; // f(x) = x²

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        TabulatedFunction derivative = operator.derive(function);

        // Проверяем количество точек
        assertEquals(4, derivative.getCount());
        // Проверяем, что x значения сохранились
        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(xValues[i], derivative.getX(i), 1e-10);
        }
    }
}