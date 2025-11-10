package operations;

import functions.MathFunction;

public class LeftSteppingDifferentialOperator extends SteppingDifferentialOperator {

    public LeftSteppingDifferentialOperator(double step) {
        super(step);
    }

    @Override
    public MathFunction derive(MathFunction function) {
        return new MathFunction() {
            @Override
            public double apply(double x) {
                // Левая разностная производная: (f(x) - f(x - step)) / step
                return (function.apply(x) - function.apply(x - step)) / step;
            }
        };
    }
}