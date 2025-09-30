package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NewtonMethodFunctionTest {
    @Test
    public void testFindRootOfQuadratic() {
        MathFunction f = x -> x * x - 4;       // f(x) = x^2 - 4
        MathFunction df = x -> 2 * x;          // f'(x) = 2x
        NewtonMethodFunction newton = new NewtonMethodFunction(f, df, 1e-6, 100);

        double root = newton.apply(1.0);       // начнём с 1
        assertEquals(2.0, root, 1e-6);         // должно сойтись к корню x=2
    }

    @Test
    public void testDerivativeZeroThrowsException() {
        MathFunction f = x -> x * x;
        MathFunction df = x -> 0.0;   // производная всегда 0
        NewtonMethodFunction newton = new NewtonMethodFunction(f, df, 1e-6, 10);

        assertThrows(ArithmeticException.class, () -> newton.apply(1.0));
    }
}
