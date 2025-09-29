package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompositeFunctionTest {

    @Test
    void testApplyWithIdentityAndSqr() {
        // f(x) = x
        MathFunction identity = new IdentityFunction();
        // g(x) = x² (квадрат)
        MathFunction sqr = new SqrFunction();

        // h(x) = g(f(x)) = (x)² = x²
        CompositeFunction comp = new CompositeFunction(identity, sqr);

        assertEquals(25.0, comp.apply(5.0), 0.0001, "5² должно быть 25");
        assertEquals(9.0, comp.apply(-3.0), 0.0001, "(-3)² должно быть 9");
        assertEquals(0.0, comp.apply(0.0), 0.0001, "0² должно быть 0");
        assertEquals(2.25, comp.apply(1.5), 0.0001, "1.5² должно быть 2.25");
    }

    @Test
    void testApplyWithSqrAndIdentity() {
        // f(x) = x²
        MathFunction sqr = new SqrFunction();
        // g(x) = x
        MathFunction identity = new IdentityFunction();

        // h(x) = g(f(x)) = (x²) = x²
        CompositeFunction comp = new CompositeFunction(sqr, identity);

        assertEquals(16.0, comp.apply(4.0), 0.0001, "4² должно быть 16");
        assertEquals(4.0, comp.apply(-2.0), 0.0001, "(-2)² должно быть 4");
    }

    @Test
    void testApplyWithTwoCustomFunctions() {
        // f(x) = x + 1
        MathFunction addOne = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        // g(x) = x * 2
        MathFunction multiplyByTwo = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * 2;
            }
        };

        // h(x) = g(f(x)) = (x + 1) * 2
        CompositeFunction comp = new CompositeFunction(addOne, multiplyByTwo);

        assertEquals(8.0, comp.apply(3.0), 0.0001, "(3+1)*2 должно быть 8");
        assertEquals(2.0, comp.apply(0.0), 0.0001, "(0+1)*2 должно быть 2");
        assertEquals(-4.0, comp.apply(-3.0), 0.0001, "(-3+1)*2 должно быть -4");
    }

    @Test
    void testCompositeOfComposites() {
        //  f(x) = x + 1 → g(x) = x * 2 = (x + 1) * 2
        MathFunction addOne = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        MathFunction multiplyByTwo = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * 2;
            }
        };

        CompositeFunction comp1 = new CompositeFunction(addOne, multiplyByTwo);

        //  h(x) = x²
        MathFunction sqr = new SqrFunction();

        // comp2(x) = sqr(comp1(x)) = ((x + 1) * 2)²
        CompositeFunction comp2 = new CompositeFunction(comp1, sqr);

        assertEquals(64.0, comp2.apply(3.0), 0.0001, "((3+1)*2)² должно быть 64");
        assertEquals(4.0, comp2.apply(0.0), 0.0001, "((0+1)*2)² должно быть 4");
        assertEquals(4.0, comp2.apply(-2.0), 0.0001, "((-2+1)*2)² должно быть 4"); // ИСПРАВЛЕНО
    }

    @Test
    void testConstructorAndFields() {
        MathFunction first = new IdentityFunction();
        MathFunction second = new SqrFunction();

        CompositeFunction comp = new CompositeFunction(first, second);

        // Проверяем, что функции правильно сохранились через их работу
        double result = comp.apply(4.0);
        assertEquals(16.0, result, 0.0001, "Должно работать с правильными функциями");
    }

    @Test
    void testWithConstantFunction() {
        // f(x) = 5
        MathFunction constant = new MathFunction() {
            @Override
            public double apply(double x) {
                return 5.0;
            }
        };

        // g(x) = x + 2
        MathFunction addTwo = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 2;
            }
        };

        // h(x) = g(f(x)) = g(5) = 5 + 2 = 7
        CompositeFunction comp = new CompositeFunction(constant, addTwo);

        assertEquals(7.0, comp.apply(10.0), 0.0001, "Должно всегда возвращать 7");
        assertEquals(7.0, comp.apply(-5.0), 0.0001, "Должно всегда возвращать 7");
        assertEquals(7.0, comp.apply(0.0), 0.0001, "Должно всегда возвращать 7");
    }

    @Test
    void testMultipleCompositions() {
        //  x → x + 1 → (x+1) * 2 → ((x+1)*2)²
        MathFunction addOne = x -> x + 1;
        MathFunction multiplyByTwo = x -> x * 2;
        MathFunction sqr = x -> x * x;

        CompositeFunction comp1 = new CompositeFunction(addOne, multiplyByTwo);
        CompositeFunction comp2 = new CompositeFunction(comp1, sqr);

        assertEquals(36.0, comp2.apply(2.0), 0.0001, "((2+1)*2)² должно быть 36");
        assertEquals(16.0, comp2.apply(1.0), 0.0001, "((1+1)*2)² должно быть 16");
        assertEquals(4.0, comp2.apply(0.0), 0.0001, "((0+1)*2)² должно быть 4");
    }

    @Test
    void testWithSameFunction() {
        // f(x) = x + 1
        MathFunction addOne = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        // h(x) = f(f(x)) = (x + 1) + 1 = x + 2
        CompositeFunction comp = new CompositeFunction(addOne, addOne);

        assertEquals(7.0, comp.apply(5.0), 0.0001, "5 + 2 должно быть 7");
        assertEquals(0.0, comp.apply(-2.0), 0.0001, "-2 + 2 должно быть 0");
        assertEquals(2.0, comp.apply(0.0), 0.0001, "0 + 2 должно быть 2");
    }

    @Test
    void testComplexChain() {
        //  x → x² → (x²) + 3 → ((x²) + 3) * 2
        MathFunction sqr = new SqrFunction();
        MathFunction addThree = x -> x + 3;
        MathFunction multiplyByTwo = x -> x * 2;

        CompositeFunction comp1 = new CompositeFunction(sqr, addThree);
        CompositeFunction comp2 = new CompositeFunction(comp1, multiplyByTwo);

        assertEquals(14.0, comp2.apply(2.0), 0.0001);

        assertEquals(8.0, comp2.apply(-1.0), 0.0001);

        assertEquals(6.0, comp2.apply(0.0), 0.0001);
    }
}