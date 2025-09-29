package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdentityFunctionTest {

    @Test
    void testApply() {
        IdentityFunction function = new IdentityFunction();
        assertEquals(5.0, function.apply(5.0), 0.0001);
        assertEquals(0.0, function.apply(0.0), 0.0001);
        assertEquals(-3.5, function.apply(-3.5), 0.0001);
    }
}