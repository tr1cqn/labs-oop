package operations;

import functions.MathFunction;

public class RightSteppingDifferentialOperator extends SteppingDifferentialOperator {

    public RightSteppingDifferentialOperator(double step) {
        super(step);
    }

    @Override
    public MathFunction derive(MathFunction function) {
        return new MathFunction() {
            @Override
            public double apply(double x) {
                // Правая разностная производная: (f(x + step) - f(x)) / step
                return (function.apply(x + step) - function.apply(x)) / step;
            }
        };
    }
}