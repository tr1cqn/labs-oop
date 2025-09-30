package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConstantFunctionTest {
    @Test
    public void testConstantAlwaysSame() {
        MathFunction f = new ConstantFunction(5.0);
        assertEquals(5.0, f.apply(0.0));
        assertEquals(5.0, f.apply(100.0));
        assertEquals(5.0, f.apply(-50.0));
    }
}
