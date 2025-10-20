package operations;

import functions.Point;
import functions.TabulatedFunction;

public class TabulatedFunctionOperationService {


    public static Point[] asPoints(TabulatedFunction function) {
        int pointCount = function.getCount();
        Point[] points = new Point[pointCount];

        int i = 0;
        // Используем цикл for-each для обхода всех точек функции
        for (Point point : function) {
            points[i] = point;
            i++;
        }

        return points;
    }
}