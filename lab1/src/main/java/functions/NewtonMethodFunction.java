package functions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NewtonMethodFunction implements MathFunction {
    private static final Logger logger = LogManager.getLogger(NewtonMethodFunction.class);
    private final MathFunction function;      // f(x)
    private final MathFunction derivative;   // f'(x)
    private final double tolerance;          // точность
    private final int maxIterations;         // максимальное число итераций

    public NewtonMethodFunction(MathFunction function, MathFunction derivative, 
                               double tolerance, int maxIterations) {
        this.function = function;
        this.derivative = derivative;
        this.tolerance = tolerance;
        this.maxIterations = maxIterations;
    }

    public NewtonMethodFunction(MathFunction function, MathFunction derivative) {
        this(function, derivative, 1e-6, 100);
    }

    @Override
    public double apply(double x0) {
        logger.info("Запуск метода Ньютона для начального приближения: {}, точность: {}, макс. итераций: {}", 
            x0, tolerance, maxIterations);
        double x = x0;
        
        for (int i = 0; i < maxIterations; i++) {
            double fx = function.apply(x);
            double fpx = derivative.apply(x);
            logger.trace("Итерация {}: x={}, f(x)={}, f'(x)={}", i, x, fx, fpx);
            
            // Проверка, чтобы избежать деления на ноль
            if (Math.abs(fpx) < 1e-12) {
                logger.error("Производная близка к нулю на итерации {}: f'(x)={}, метод Ньютона не может продолжить", 
                    i, fpx);
                throw new ArithmeticException("Производная близка к нулю, метод Ньютона не может продолжить");
            }
            
            double xNew = x - fx / fpx;
            double delta = Math.abs(xNew - x);
            logger.debug("Итерация {}: x={} -> xNew={}, изменение: {}", i, x, xNew, delta);
            
            // Проверка достижения точности
            if (delta < tolerance) {
                logger.info("Метод Ньютона сошелся за {} итераций, результат: {}", i + 1, xNew);
                return xNew;
            }
            
            x = xNew;
        }
        
        logger.error("Метод Ньютона не сошелся за {} итераций, последнее значение: {}", maxIterations, x);
        throw new RuntimeException("Метод Ньютона не сошелся за " + maxIterations + " итераций");
    }

    public MathFunction getFunction() {
        return function;
    }

    public MathFunction getDerivative() {
        return derivative;
    }

    public double getTolerance() {
        return tolerance;
    }

    public int getMaxIterations() {
        return maxIterations;
    }
}
