package operations;
import functions.MathFunction;
public interface DifferentialOperator<T extends MathFunction> {
    T derive(T function);
}
