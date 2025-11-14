package operations;

import functions.MathFunction;
import functions.SqrFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SteppingDifferentialOperatorTest {

    private static final double DELTA = 0.1; // Допустимая погрешность

    @Test
    public void testLeftSteppingOperatorWithSqrFunction() {
        // Для f(x) = x² производная f'(x) = 2x
        // Левая производная в точке x=2: (f(2) - f(2-0.1)) / 0.1
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(0.1);
        MathFunction sqrFunction = new SqrFunction();
        MathFunction derivative = operator.derive(sqrFunction);

        // Ожидаем: (4 - 3.61) / 0.1 = 0.39 / 0.1 = 3.9
        double result = derivative.apply(2.0);
        assertEquals(3.9, result, DELTA);
    }

    @Test
    public void testRightSteppingOperatorWithSqrFunction() {
        // Для f(x) = x² производная f'(x) = 2x
        // Правая производная в точке x=2: (f(2+0.1) - f(2)) / 0.1
        RightSteppingDifferentialOperator operator = new RightSteppingDifferentialOperator(0.1);
        MathFunction sqrFunction = new SqrFunction();
        MathFunction derivative = operator.derive(sqrFunction);

        // Ожидаем: (4.41 - 4) / 0.1 = 0.41 / 0.1 = 4.1
        double result = derivative.apply(2.0);
        assertEquals(4.1, result, DELTA);
    }

    @Test
    public void testMiddleSteppingOperatorWithSqrFunction() {
        // Для f(x) = x² производная f'(x) = 2x
        // Средняя производная в точке x=2: (f(2+0.1) - f(2-0.1)) / (2 * 0.1)
        MiddleSteppingDifferentialOperator operator = new MiddleSteppingDifferentialOperator(0.1);
        MathFunction sqrFunction = new SqrFunction();
        MathFunction derivative = operator.derive(sqrFunction);

        // Ожидаем: (4.41 - 3.61) / 0.2 = 0.8 / 0.2 = 4.0
        double result = derivative.apply(2.0);
        assertEquals(4.0, result, DELTA);
    }

    @Test
    public void testConstructorThrowsExceptionForInvalidStep() {
        // Отрицательный шаг
        assertThrows(IllegalArgumentException.class, () -> {
            new LeftSteppingDifferentialOperator(-1.0);
        });

        // Нулевой шаг
        assertThrows(IllegalArgumentException.class, () -> {
            new LeftSteppingDifferentialOperator(0.0);
        });

        // Бесконечность
        assertThrows(IllegalArgumentException.class, () -> {
            new LeftSteppingDifferentialOperator(Double.POSITIVE_INFINITY);
        });

        // NaN
        assertThrows(IllegalArgumentException.class, () -> {
            new LeftSteppingDifferentialOperator(Double.NaN);
        });
    }

    @Test
    public void testSetStepThrowsExceptionForInvalidStep() {
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(0.1);

        assertThrows(IllegalArgumentException.class, () -> {
            operator.setStep(-1.0);
        });
    }

    @Test
    public void testGetStep() {
        double step = 0.5;
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(step);
        assertEquals(step, operator.getStep(), 1e-10);
    }

    @Test
    public void testSetStep() {
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(0.1);
        operator.setStep(0.2);
        assertEquals(0.2, operator.getStep(), 1e-10);
    }


}