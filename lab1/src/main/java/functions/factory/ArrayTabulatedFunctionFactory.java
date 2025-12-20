package functions.factory;

import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ArrayTabulatedFunctionFactory implements TabulatedFunctionFactory {
    private static final Logger logger = LogManager.getLogger(ArrayTabulatedFunctionFactory.class);
    
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        logger.debug("Создание ArrayTabulatedFunction через фабрику, количество точек: {}", xValues.length);
        return new ArrayTabulatedFunction(xValues, yValues);
    }

}
