package functions;

public class SimpleIterationFunction implements MathFunction {
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
        double current = initialGuess;
        for (int i = 0; i < maxIterations; i++) {
            double next = phiFunction.apply(current);

            if (Math.abs(next - current) < tolerance) {
                return next;
            }

            current = next;
        }
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