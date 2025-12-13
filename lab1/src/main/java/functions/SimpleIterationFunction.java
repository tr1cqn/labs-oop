package functions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleIterationFunction implements MathFunction {
    private static final Logger logger = LogManager.getLogger(SimpleIterationFunction.class);
    private final MathFunction phiFunction;
    private final double initialGuess;
    private final int maxIterations;
    private final double tolerance;

    public SimpleIterationFunction(MathFunction phiFunction, double initialGuess, int maxIterations, double tolerance) {
        this.phiFunction = phiFunction;
        this.initialGuess = initialGuess;
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
    }

    public SimpleIterationFunction(MathFunction phiFunction, double initialGuess) {
        this(phiFunction, initialGuess, 100, 1e-6);
    }

    @Override
    public double apply(double x) {
        logger.info("Запуск метода простой итерации для x={}, начальное приближение: {}, точность: {}, макс. итераций: {}", 
            x, initialGuess, tolerance, maxIterations);
        double current = initialGuess;
        for (int i = 0; i < maxIterations; i++) {
            double next = phiFunction.apply(current);
            double delta = Math.abs(next - current);
            logger.trace("Итерация {}: current={}, next={}, изменение: {}", i, current, next, delta);

            if (delta < tolerance) {
                logger.info("Метод простой итерации сошелся за {} итераций, результат: {}", i + 1, next);
                return next;
            }

            current = next;
        }
        logger.warn("Метод простой итерации не достиг требуемой точности за {} итераций, возвращается последнее значение: {}", 
            maxIterations, current);
        return current;
    }
    public MathFunction getPhiFunction() {
        return phiFunction;
    }

    public double getInitialGuess() {
        return initialGuess;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public double getTolerance() {
        return tolerance;
    }
}