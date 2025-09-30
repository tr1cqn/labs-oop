package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MathFunctionAndThenTest {
    @Test
    public void testAndThenComposition() {
        MathFunction sqr = new SqrFunction();       // x^2
        MathFunction constFive = new ConstantFunction(5.0); // f(x)=5

        MathFunction composed = sqr.andThen(constFive); // (x^2)->5

        assertEquals(5.0, composed.apply(10.0));
        assertEquals(5.0, composed.apply(-7.0));
    }
}
