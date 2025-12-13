package functions.factory;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LinkedListTabulatedFunctionFactory implements TabulatedFunctionFactory {
    private static final Logger logger = LogManager.getLogger(LinkedListTabulatedFunctionFactory.class);
    
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        logger.debug("Создание LinkedListTabulatedFunction через фабрику, количество точек: {}", xValues.length);
        return new LinkedListTabulatedFunction(xValues, yValues);
    }
}
