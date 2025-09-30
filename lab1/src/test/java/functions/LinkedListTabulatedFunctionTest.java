package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListComplexFunctionsTest {

    @Test
    void testCompositeOfTwoLinkedListFunctions() {
        // Первая функция f(x) = 4x
        double[] xValues1 = {0.0, 1.0, 2.0};
        double[] yValues1 = {0.0, 4.0, 8.0};
        LinkedListTabulatedFunction listFunc1 = new LinkedListTabulatedFunction(xValues1, yValues1);

        // Вторая функция g(x) = x/2
        double[] xValues2 = {0.0, 4.0, 8.0};
        double[] yValues2 = {0.0, 2.0, 4.0};
        LinkedListTabulatedFunction listFunc2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        // h(x) = g(f(x)) = g(4x) = (4x)/2 = 2x
        CompositeFunction composite = new CompositeFunction(listFunc1, listFunc2);

        // x=1.0: f(1.0)=4.0, g(4.0)=2.0
        assertEquals(2.0, composite.apply(1.0), 1e-10);
        //  x=2.0: f(2.0)=8.0, g(8.0)=4.0
        assertEquals(4.0, composite.apply(2.0), 1e-10);
    }

    @Test
    void testCompositeOfLinkedListAndMathFunction() {
        //  f(x) = x²
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction listFunc = new LinkedListTabulatedFunction(xValues, yValues);

        // g(x) = x²
        MathFunction mathFunc = new SqrFunction();

        // h(x) = g(f(x)) = g(x²) = (x²)² = x⁴
        CompositeFunction composite = new CompositeFunction(listFunc, mathFunc);

        // x=1.0: f(1.0)=1.0, g(1.0)=1.0
        assertEquals(1.0, composite.apply(1.0), 1e-10);
        //  x=2.0: f(2.0)=4.0, g(4.0)=16.0
        assertEquals(16.0, composite.apply(2.0), 1e-10);
    }

    @Test
    void testCompositeOfMathFunctionAndLinkedList() {
        //  f(x) = x
        MathFunction mathFunc = new IdentityFunction();

        //  g(x) = 2x
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 2.0, 4.0};
        LinkedListTabulatedFunction listFunc = new LinkedListTabulatedFunction(xValues, yValues);

        // h(x) = g(f(x)) = g(x) = 2x
        CompositeFunction composite = new CompositeFunction(mathFunc, listFunc);

        // Проверяем: для x=1.0: f(1.0)=1.0, g(1.0)=2.0
        assertEquals(2.0, composite.apply(1.0), 1e-10);
        // Проверяем: для x=2.0: f(2.0)=2.0, g(2.0)=4.0
        assertEquals(4.0, composite.apply(2.0), 1e-10);
    }

    @Test
    void testTripleCompositeLinkedListFunctions() {
        // f(x) = 3x
        double[] xValues1 = {0.0, 1.0, 2.0};
        double[] yValues1 = {0.0, 3.0, 6.0};
        LinkedListTabulatedFunction func1 = new LinkedListTabulatedFunction(xValues1, yValues1);

        // g(x) = x/3
        double[] xValues2 = {0.0, 3.0, 6.0};
        double[] yValues2 = {0.0, 1.0, 2.0};
        LinkedListTabulatedFunction func2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        // h(x) = 2x
        double[] xValues3 = {0.0, 1.0, 2.0};
        double[] yValues3 = {0.0, 2.0, 4.0};
        LinkedListTabulatedFunction func3 = new LinkedListTabulatedFunction(xValues3, yValues3);

        //  k(x) = h(g(f(x))) = h(g(3x)) = h(x) = 2x
        CompositeFunction comp1 = new CompositeFunction(func1, func2); // g(f(x)) = x
        CompositeFunction comp2 = new CompositeFunction(comp1, func3); // h(x) = 2x

        //  x=1.0: f(1.0)=3.0, g(3.0)=1.0, h(1.0)=2.0
        assertEquals(2.0, comp2.apply(1.0), 1e-10);
        //  x=2.0: f(2.0)=6.0, g(6.0)=2.0, h(2.0)=4.0
        assertEquals(4.0, comp2.apply(2.0), 1e-10);
    }

    @Test
    void testLinkedListWithInterpolationInComposite() {
        // f(x) = 2x
        double[] xValues1 = {0.0, 1.0, 2.0};
        double[] yValues1 = {0.0, 2.0, 4.0};
        LinkedListTabulatedFunction func1 = new LinkedListTabulatedFunction(xValues1, yValues1);

        //  g(x) = 5x
        double[] xValues2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValues2 = {0.0, 5.0, 10.0, 15.0, 20.0};
        LinkedListTabulatedFunction func2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        //  h(x) = g(f(x)) = g(2x) = 5*(2x) = 10x
        CompositeFunction composite = new CompositeFunction(func1, func2);

        // Для x=0.5: f(0.5)=1.0 , g(1.0)=5.0
        assertEquals(5.0, composite.apply(0.5), 1e-10);
        // Для x=1.0: f(1.0)=2.0, g(2.0)=10.0
        assertEquals(10.0, composite.apply(1.0), 1e-10);
        // Для x=1.5: f(1.5)=3.0, g(3.0)=15.0
        assertEquals(15.0, composite.apply(1.5), 1e-10);
    }

    @Test
    void testLinkedListWithExtrapolationInComposite() {
        // f(x) = x²
        double[] xValues1 = {-1.0, 0.0, 1.0};
        double[] yValues1 = {1.0, 0.0, 1.0};
        LinkedListTabulatedFunction func1 = new LinkedListTabulatedFunction(xValues1, yValues1);

        //  g(x) = 3x
        double[] xValues2 = {0.0, 1.0, 2.0};
        double[] yValues2 = {0.0, 3.0, 6.0};
        LinkedListTabulatedFunction func2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        //  h(x) = g(f(x)) = g(x²) = 3x²
        CompositeFunction composite = new CompositeFunction(func1, func2);

        // Проверяем экстраполяцию за пределами таблицы
        double result1 = composite.apply(-2.0); // f(-2.0)=4.0 (экстраполяция), g(4.0)=12.0
        double result2 = composite.apply(2.0);  // f(2.0)=4.0 (экстраполяция), g(4.0)=12.0

        assertTrue(result1 > 0.0);
        assertTrue(result2 > 0.0);
    }

    @Test
    void testLinkedListIdentityComposite() {
        // f(x) = x
        MathFunction identity = new IdentityFunction();

        //  g(x) = 5x
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 5.0, 10.0};
        LinkedListTabulatedFunction listFunc = new LinkedListTabulatedFunction(xValues, yValues);

        // Две композиции с тождественной функцией
        CompositeFunction composite1 = new CompositeFunction(identity, listFunc); // g(f(x)) = g(x) = 5x
        CompositeFunction composite2 = new CompositeFunction(listFunc, identity); // f(g(x)) = g(x) = 5x

        // Обе композиции должны давать одинаковый результат
        // Для x=1.0: результат должен быть 5.0
        assertEquals(5.0, composite1.apply(1.0), 1e-10);
        assertEquals(5.0, composite2.apply(1.0), 1e-10);
    }

    @Test
    void testLinkedListComplexInterpolationChain() {
        // f(x) = 4x
        double[] xValues1 = {0.0, 0.5, 1.0};
        double[] yValues1 = {0.0, 2.0, 4.0};
        LinkedListTabulatedFunction func1 = new LinkedListTabulatedFunction(xValues1, yValues1);

        // g(x) = x
        double[] xValues2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValues2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        LinkedListTabulatedFunction func2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        //  h(x) = g(f(x)) = g(4x) = 4x
        CompositeFunction composite = new CompositeFunction(func1, func2);

        // Для x=0.25 f(0.25)=1.0 (интерполяция), g(1.0)=1.0
        assertEquals(1.0, composite.apply(0.25), 1e-10);
        // Для x=0.5 f(0.5)=2.0, g(2.0)=2.0
        assertEquals(2.0, composite.apply(0.5), 1e-10);
        // Для x=0.75 f(0.75)=3.0 (интерполяция), g(3.0)=3.0
        assertEquals(3.0, composite.apply(0.75), 1e-10);
    }

    @Test
    void testLinkedListWithConstantFunction() {
        // Постоянная функция всегда возвращает 5.0
        MathFunction constant = new MathFunction() {
            @Override
            public double apply(double x) {
                return 5.0;
            }
        };

        // g(x) = 0.4x
        double[] xValues = {0.0, 5.0, 10.0};
        double[] yValues = {0.0, 2.0, 4.0};
        LinkedListTabulatedFunction listFunc = new LinkedListTabulatedFunction(xValues, yValues);

        // Две композиции с постоянной функцией
        CompositeFunction comp1 = new CompositeFunction(constant, listFunc); // listFunc(5.0) = 2.0
        CompositeFunction comp2 = new CompositeFunction(listFunc, constant); // constant(g(x)) = 5.0

        // Проверяем композицию 1 для любого x: constant(x)=5.0, listFunc(5.0)=2.0
        assertEquals(2.0, comp1.apply(100.0), 1e-10);
        // Проверяем композицию 2: для любого x: listFunc(x)=некоторое значение, constant(значение)=5.0
        assertEquals(5.0, comp2.apply(1.0), 1e-10);
    }

    @Test
    void testLinkedListSelfComposite() {
        // f(x) = 2x
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 2.0, 4.0};
        LinkedListTabulatedFunction listFunc = new LinkedListTabulatedFunction(xValues, yValues);

        // f(f(x)) = f(2x) = 2*(2x) = 4x
        CompositeFunction composite = new CompositeFunction(listFunc, listFunc);

        //  для x=1.0: f(1.0)=2.0, f(2.0)=4.0
        assertEquals(4.0, composite.apply(1.0), 1e-10);
        // для x=2.0: f(2.0)=4.0, f(4.0)=8.0 (экстраполяция)
        assertEquals(8.0, composite.apply(2.0), 1e-10);
    }
// Тесты для remove
        @Test
        void testRemoveFromMiddle() {
            // Тест удаления элемента из середины списка
            double[] xValues = {1.0, 2.0, 3.0, 4.0};
            double[] yValues = {10.0, 20.0, 30.0, 40.0};
            LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

            func.remove(1);
            // Проверяем, что количество элементов уменьшилось
            assertEquals(3, func.getCount());
            // Проверяем правильность порядка оставшихся элементов
            assertEquals(1.0, func.getX(0), 1e-10);
            assertEquals(3.0, func.getX(1), 1e-10);
            assertEquals(4.0, func.getX(2), 1e-10);
        }

        @Test
        void testRemoveFromBeginning() {
            // Тест удаления первого элемента списка
            double[] xValues = {1.0, 2.0, 3.0};
            double[] yValues = {10.0, 20.0, 30.0};
            LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

            // Удаляем первый элемент (индекс 0)
            func.remove(0);

            // Проверяем количество элементов и порядок
            assertEquals(2, func.getCount());
            assertEquals(2.0, func.getX(0), 1e-10);
            assertEquals(3.0, func.getX(1), 1e-10);
            assertEquals(2.0, func.leftBound(), 1e-10);
        }

        @Test
        void testRemoveFromEnd() {
            // Тест удаления последнего элемента списка
            double[] xValues = {1.0, 2.0, 3.0};
            double[] yValues = {10.0, 20.0, 30.0};
            LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

            // Удаляем последний элемент (индекс 2)
            func.remove(2);

            // Проверяем количество элементов и порядок
            assertEquals(2, func.getCount());
            assertEquals(1.0, func.getX(0), 1e-10);
            assertEquals(2.0, func.getX(1), 1e-10);
            assertEquals(2.0, func.rightBound(), 1e-10);
        }

        @Test
        void testRemoveInvalidIndex() {
            // Тест обработки некорректных индексов
            double[] xValues = {1.0, 2.0, 3.0};
            double[] yValues = {10.0, 20.0, 30.0};
            LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

            // Отрицательный индекс должен вызывать исключение
            assertThrows(IndexOutOfBoundsException.class, () -> func.remove(-1));
            // Индекс больше размера списка должен вызывать исключение
            assertThrows(IndexOutOfBoundsException.class, () -> func.remove(5));
        }

        @Test
        void testRemoveAndFunctionality() {
            // Тест работы функции после удаления элемента
            double[] xValues = {1.0, 2.0, 3.0, 4.0};
            double[] yValues = {10.0, 20.0, 30.0, 40.0};
            LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

            // Удаляем средний элемент
            func.remove(1);

            // Проверяем, что функция продолжает работать корректно
            assertEquals(3, func.getCount());
            // Проверяем значения в оставшихся точках
            assertEquals(10.0, func.apply(1.0), 1e-10);
            assertEquals(30.0, func.apply(3.0), 1e-10);
            assertEquals(40.0, func.apply(4.0), 1e-10);
        }


        @Test
        void testRemoveAndBoundsUpdate() {
            // Тест обновления границ функции после удаления
            double[] xValues = {1.0, 2.0, 3.0};
            double[] yValues = {10.0, 20.0, 30.0};
            LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);

            // Удаляем первый элемент - левая граница должна обновиться
            func.remove(0);
            assertEquals(2.0, func.leftBound(), 1e-10);

            // Удаляем последний элемент - правая граница должна обновиться
            func.remove(1);
            assertEquals(2.0, func.rightBound(), 1e-10);
        }
}