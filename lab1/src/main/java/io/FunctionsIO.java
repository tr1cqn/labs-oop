package io;

import functions.TabulatedFunction;
import functions.Point;
import java.io.*;
import functions.factory.TabulatedFunctionFactory;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FunctionsIO {
    private static final Logger logger = LogManager.getLogger(FunctionsIO.class);

    private FunctionsIO() {
        throw new UnsupportedOperationException("Нельзя создавать экземпляры утилитного класса FunctionsIO");
    }

    public static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function)
            throws IOException {
        logger.info("Запись TabulatedFunction в бинарный поток, количество точек: {}", function.getCount());

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        // Записываем количество точек
        dataOutputStream.writeInt(function.getCount());
        logger.debug("Записано количество точек: {}", function.getCount());

        // Записываем все точки (x, y)
        int pointCount = 0;
        for (Point point : function) {
            dataOutputStream.writeDouble(point.x);
            dataOutputStream.writeDouble(point.y);
            pointCount++;
        }
        logger.debug("Записано точек: {}", pointCount);
        dataOutputStream.flush();
        logger.info("Запись TabulatedFunction в бинарный поток завершена успешно");
    }

    public static TabulatedFunction readTabulatedFunction(BufferedInputStream inputStream,
                                                          TabulatedFunctionFactory factory)
            throws IOException {
        logger.info("Чтение TabulatedFunction из бинарного потока");

        DataInputStream dataInputStream = new DataInputStream(inputStream);

        int count = dataInputStream.readInt();
        logger.debug("Прочитано количество точек: {}", count);

        if (count < 2) {
            logger.error("Некорректное количество точек при чтении: {}", count);
            throw new IOException("Количество точек должно быть не менее 2");
        }

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        for (int i = 0; i < count; i++) {
            xValues[i] = dataInputStream.readDouble();
            yValues[i] = dataInputStream.readDouble();
        }
        logger.debug("Прочитано {} точек, границы: [{}, {}]", count, xValues[0], xValues[count - 1]);

        TabulatedFunction result = factory.create(xValues, yValues);
        logger.info("TabulatedFunction успешно прочитана из бинарного потока, тип: {}", 
            result.getClass().getSimpleName());
        return result;
    }



    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws IOException {
        logger.info("Запись TabulatedFunction в текстовый поток, количество точек: {}", function.getCount());
        PrintWriter printWriter = new PrintWriter(writer);

        // Записываем количество точек
        printWriter.println(function.getCount());
        logger.debug("Записано количество точек: {}", function.getCount());

        // Записываем все точки (x, y) через пробел
        int pointCount = 0;
        for (Point point : function) {
            printWriter.printf("%f %f\n", point.x, point.y);
            pointCount++;
        }
        logger.debug("Записано точек: {}", pointCount);
        printWriter.flush(); // Сбрасываем буфер, но не закрываем поток!
        logger.info("Запись TabulatedFunction в текстовый поток завершена успешно");
    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader, TabulatedFunctionFactory factory)
            throws IOException {
        logger.info("Чтение TabulatedFunction из текстового потока");
        try {
            // Читаем количество точек
            String countLine = reader.readLine();
            if (countLine == null) {
                logger.error("Файл пуст при попытке чтения функции");
                throw new IOException("Файл пуст");
            }
            int count = Integer.parseInt(countLine.trim());
            logger.debug("Прочитано количество точек: {}", count);

            if (count < 2) {
                logger.error("Некорректное количество точек при чтении: {}", count);
                throw new IOException("Количество точек должно быть не менее 2");
            }

            double[] xValues = new double[count];
            double[] yValues = new double[count];

            // Создаем форматтер для чисел с запятой
            NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

            // Читаем точки
            for (int i = 0; i < count; i++) {
                String line = reader.readLine();
                if (line == null) {
                    logger.error("Неожиданный конец файла при чтении точки с индексом {}", i);
                    throw new IOException("Неожиданный конец файла");
                }

                String[] parts = line.split(" ");
                if (parts.length != 2) {
                    logger.error("Некорректный формат строки при чтении точки {}: {}", i, line);
                    throw new IOException("Некорректный формат строки: " + line);
                }

                try {
                    xValues[i] = formatter.parse(parts[0]).doubleValue();
                    yValues[i] = formatter.parse(parts[1]).doubleValue();
                } catch (ParseException e) {
                    logger.error("Ошибка парсинга числа в строке {}: {}", i, line, e);
                    throw new IOException("Ошибка парсинга числа", e);
                }
            }
            logger.debug("Прочитано {} точек, границы: [{}, {}]", count, xValues[0], xValues[count - 1]);

            TabulatedFunction result = factory.create(xValues, yValues);
            logger.info("TabulatedFunction успешно прочитана из текстового потока, тип: {}", 
                result.getClass().getSimpleName());
            return result;

        } catch (NumberFormatException e) {
            logger.error("Некорректный формат числа при чтении функции", e);
            throw new IOException("Некорректный формат числа", e);
        }
    }

    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        logger.info("Сериализация TabulatedFunction, тип: {}, количество точек: {}", 
            function.getClass().getSimpleName(), function.getCount());
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(function);
        oos.flush(); // Сбрасываем буфер, но не закрываем поток!
        logger.info("Сериализация TabulatedFunction завершена успешно");
    }
    // метод десериализации
    public static TabulatedFunction deserialize(BufferedInputStream stream)
            throws IOException, ClassNotFoundException {
        logger.info("Десериализация TabulatedFunction");
        ObjectInputStream objectInputStream = new ObjectInputStream(stream);
        TabulatedFunction result = (TabulatedFunction) objectInputStream.readObject();
        logger.info("TabulatedFunction успешно десериализована, тип: {}, количество точек: {}", 
            result.getClass().getSimpleName(), result.getCount());
        return result;
    }
}