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
    @Test
    void testApplyWithZero() {
        IdentityFunction function = new IdentityFunction();
        //  f(0) = 0
        assertEquals(0.0, function.apply(0.0), 1e-10);
    }
    @Test
    void testApplyWithDecimalNumbers() {
        IdentityFunction function = new IdentityFunction();
        // Проверяем работу с дробными числами
        assertEquals(2.5, function.apply(2.5), 1e-10);
        assertEquals(0.333333, function.apply(0.333333), 1e-10);
        assertEquals(-7.89, function.apply(-7.89), 1e-10);
    }


}

