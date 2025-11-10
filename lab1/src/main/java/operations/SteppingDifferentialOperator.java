package operations;

import functions.MathFunction;

public abstract class SteppingDifferentialOperator implements DifferentialOperator<MathFunction> {

    protected double step;

    public SteppingDifferentialOperator(double step) {
        // Проверка корректности шага
        if (step <= 0 || Double.isInfinite(step) || Double.isNaN(step)) {
            throw new IllegalArgumentException("Шаг должен быть положительным числом: " + step);
        }
        this.step = step;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        // Та же проверка для сеттера
        if (step <= 0 || Double.isInfinite(step) || Double.isNaN(step)) {
            throw new IllegalArgumentException("Шаг должен быть положительным числом: " + step);
        }
        this.step = step;
    }
}