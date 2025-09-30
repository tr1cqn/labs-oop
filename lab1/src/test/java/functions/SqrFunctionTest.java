package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SqrFunctionTest {
    @Test
    public void testPositiveNumbers() {
        MathFunction f = new SqrFunction();
        assertEquals(9.0, f.apply(3.0), 1e-9);
        assertEquals(4.0, f.apply(2.0), 1e-9);
    }

    @Test
    public void testNegativeNumbers() {
        MathFunction f = new SqrFunction();
        assertEquals(9.0, f.apply(-3.0), 1e-9);
    }

    @Test
    public void testZero() {
        MathFunction f = new SqrFunction();
        assertEquals(0.0, f.apply(0.0), 1e-9);
    }
}
