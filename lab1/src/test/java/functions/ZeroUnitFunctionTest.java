package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ZeroUnitFunctionTest {
    @Test
    public void testZeroFunction() {
        MathFunction f = new ZeroFunction();
        assertEquals(0.0, f.apply(-10.0));
        assertEquals(0.0, f.apply(123.45));
    }

    @Test
    public void testUnitFunction() {
        MathFunction f = new UnitFunction();
        assertEquals(1.0, f.apply(-10.0));
        assertEquals(1.0, f.apply(123.45));
    }
}
