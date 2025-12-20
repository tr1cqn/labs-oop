package functions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CompositeFunction implements MathFunction {
    private static final Logger logger = LogManager.getLogger(CompositeFunction.class);
    private final MathFunction firstFunction;
    private final MathFunction secondFunction;

    public CompositeFunction(MathFunction first, MathFunction second) {
        this.firstFunction = first;
        this.secondFunction = second;
        logger.debug("Создана композитная функция: {} o {}", 
            first.getClass().getSimpleName(), second.getClass().getSimpleName());
    }

    @Override
    public double apply(double x) {
        logger.trace("Вычисление композитной функции для x={}", x);
        double intermediate = firstFunction.apply(x);
        logger.trace("Результат первой функции: f1({})={}", x, intermediate);
        double result = secondFunction.apply(intermediate);
        logger.trace("Результат композитной функции: f2(f1({}))={}", x, result);
        return result;
    }
}