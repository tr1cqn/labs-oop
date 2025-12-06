package concurrent;

import functions.*;
import functions.factory.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SynchronizedTabulatedFunctionOperationTest {

    @Test
    void testDoSynchronouslyWithReturnValue() {
        TabulatedFunction baseFunction = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{4, 5, 6}
        );
        SynchronizedTabulatedFunction func = new SynchronizedTabulatedFunction(baseFunction);

        SynchronizedTabulatedFunction.Operation<Double> op =
                new SynchronizedTabulatedFunction.Operation<Double>() {
                    public Double apply(SynchronizedTabulatedFunction f) {
                        return f.getY(0) + f.getY(1) + f.getY(2);
                    }
                };

        Double result = func.doSynchronously(op);
        assertEquals(15.0, result);
    }

    @Test
    void testDoSynchronouslyWithVoid() {
        TabulatedFunction baseFunction = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{1, 1, 1}
        );
        SynchronizedTabulatedFunction func = new SynchronizedTabulatedFunction(baseFunction);

        SynchronizedTabulatedFunction.Operation<Void> op =
                new SynchronizedTabulatedFunction.Operation<Void>() {
                    public Void apply(SynchronizedTabulatedFunction f) {
                        for (int i = 0; i < f.getCount(); i++) {
                            f.setY(i, f.getY(i) * 2);
                        }
                        return null;
                    }
                };

        Void result = func.doSynchronously(op);
        assertNull(result);
        assertEquals(2.0, func.getY(0));
        assertEquals(2.0, func.getY(1));
        assertEquals(2.0, func.getY(2));
    }
}