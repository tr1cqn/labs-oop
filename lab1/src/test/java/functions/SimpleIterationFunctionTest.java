package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimpleIterationFunctionTest {

    @Test
    void testSquareRootOfTwo() {
        MathFunction phi = new MathFunction() {
            @Override
            public double apply(double x) {
                return (x + 2 / x) / 2;
            }
        };

        SimpleIterationFunction solver = new SimpleIterationFunction(phi, 1.0, 50, 1e-10);
        double result = solver.apply(0);  // Аргумент не важен

        assertEquals(Math.sqrt(2), result, 1e-6, "Должен найти √2");
    }

    @Test
    void testLinearEquation() {
        MathFunction phi = new MathFunction() {
            @Override
            public double apply(double x) {
                return 0.5 * x + 1;
            }
        };

        SimpleIterationFunction solver = new SimpleIterationFunction(phi, 0.0, 50, 1e-10);
        double result = solver.apply(0);

        assertEquals(2.0, result, 1e-6, "Должен найти корень x = 2");
    }

    @Test
    void testConvergenceToFixedPoint() {
        MathFunction phi = new MathFunction() {
            @Override
            public double apply(double x) {
                return 0.5 * x;
            }
        };

        SimpleIterationFunction solver = new SimpleIterationFunction(phi, 10.0, 50, 1e-10);
        double result = solver.apply(0);

        assertEquals(0.0, result, 1e-6, "Должен сойтись к 0");
    }

    @Test
    void testMaxIterationsReached() {
        MathFunction phi = new MathFunction() {
            @Override
            public double apply(double x) {
                return -x;
            }
        };

        SimpleIterationFunction solver = new SimpleIterationFunction(phi, 1.0, 5, 1e-10);
        double result = solver.apply(0);
        assertTrue(Math.abs(result) > 0, "Должен вернуть последнее значение при отсутствии сходимости");
    }

    @Test
    void testGetters() {
        MathFunction phi = new IdentityFunction();
        SimpleIterationFunction solver = new SimpleIterationFunction(phi, 1.0, 100, 1e-8);

        assertEquals(phi, solver.getPhiFunction());
        assertEquals(1.0, solver.getInitialGuess(), 1e-10);
        assertEquals(100, solver.getMaxIterations());
        assertEquals(1e-8, solver.getTolerance(), 1e-10);
    }
}