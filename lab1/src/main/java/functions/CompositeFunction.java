package functions;

public class CompositeFunction implements MathFunction {
    private final MathFunction firstFunction;
    private final MathFunction secondFunction;

    public CompositeFunction(MathFunction first, MathFunction second) {
        this.firstFunction = first;
        this.secondFunction = second;
    }

    @Override
    public double apply(double x) {
        return secondFunction.apply(firstFunction.apply(x));
    }
}