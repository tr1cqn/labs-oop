package io;

import functions.TabulatedFunction;
import functions.Point;
import java.io.*;
import functions.factory.TabulatedFunctionFactory;

public final class FunctionsIO {

    private FunctionsIO() {
        throw new UnsupportedOperationException("Нельзя создавать экземпляры утилитного класса FunctionsIO");
    }
    public static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function)
            throws IOException {

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        // Записываем количество точек
        dataOutputStream.writeInt(function.getCount());

        // Записываем все точки (x, y)
        for (Point point : function) {
            dataOutputStream.writeDouble(point.x);
            dataOutputStream.writeDouble(point.y);
        }

        dataOutputStream.flush();
    }

    public static TabulatedFunction readTabulatedFunction(BufferedInputStream inputStream,
                                                          TabulatedFunctionFactory factory)
            throws IOException {

        DataInputStream dataInputStream = new DataInputStream(inputStream);

        int count = dataInputStream.readInt();

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        for (int i = 0; i < count; i++) {
            xValues[i] = dataInputStream.readDouble();
            yValues[i] = dataInputStream.readDouble();
        }

        return factory.create(xValues, yValues);
    }

}