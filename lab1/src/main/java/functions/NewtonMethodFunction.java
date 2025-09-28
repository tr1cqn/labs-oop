package functions;

public class NewtonMethodFunction implements MathFunction {
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
        double x = x0;
        
        for (int i = 0; i < maxIterations; i++) {
            double fx = function.apply(x);
            double fpx = derivative.apply(x);
            
            // Проверка, чтобы избежать деления на ноль
            if (Math.abs(fpx) < 1e-12) {
                throw new ArithmeticException("Производная близка к нулю, метод Ньютона не может продолжить");
            }
            
            double xNew = x - fx / fpx;
            
            // Проверка достижения точности
            if (Math.abs(xNew - x) < tolerance) {
                return xNew;
            }
            
            x = xNew;
        }
        
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
