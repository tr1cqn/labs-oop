package io;

import functions.TabulatedFunction;
import functions.Point;
import java.io.*;
import functions.factory.TabulatedFunctionFactory;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

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



    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws IOException {
        PrintWriter printWriter = new PrintWriter(writer);

        // Записываем количество точек
        printWriter.println(function.getCount());

        // Записываем все точки (x, y) через пробел
        for (Point point : function) {
            printWriter.printf("%f %f\n", point.x, point.y);
        }

        printWriter.flush(); // Сбрасываем буфер, но не закрываем поток!
    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader, TabulatedFunctionFactory factory)
            throws IOException {
        try {
            // Читаем количество точек
            String countLine = reader.readLine();
            if (countLine == null) {
                throw new IOException("Файл пуст");
            }
            int count = Integer.parseInt(countLine.trim());

            double[] xValues = new double[count];
            double[] yValues = new double[count];

            // Создаем форматтер для чисел с запятой
            NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

            // Читаем точки
            for (int i = 0; i < count; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Неожиданный конец файла");
                }

                String[] parts = line.split(" ");
                if (parts.length != 2) {
                    throw new IOException("Некорректный формат строки: " + line);
                }

                try {
                    xValues[i] = formatter.parse(parts[0]).doubleValue();
                    yValues[i] = formatter.parse(parts[1]).doubleValue();
                } catch (ParseException e) {
                    throw new IOException("Ошибка парсинга числа", e);
                }
            }

            return factory.create(xValues, yValues);

        } catch (NumberFormatException e) {
            throw new IOException("Некорректный формат числа", e);
        }
    }

    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(function);
        oos.flush(); // Сбрасываем буфер, но не закрываем поток!
    }
    // метод десериализации
    public static TabulatedFunction deserialize(BufferedInputStream stream)
            throws IOException, ClassNotFoundException {

        ObjectInputStream objectInputStream = new ObjectInputStream(stream);
        return (TabulatedFunction) objectInputStream.readObject();
    }
}