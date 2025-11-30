package io;

import functions.*;
import functions.factory.*;
import operations.TabulatedDifferentialOperator;
import java.io.*;

public class LinkedListTabulatedFunctionSerialization {

    public static void main(String[] args) {



        // Создаем исходную функцию типа LinkedListTabulatedFunction
        double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 3.0, 5.0, 7.0, 9.0};

        LinkedListTabulatedFunction originalFunction = new LinkedListTabulatedFunction(xValues, yValues);

        // Находим первую и вторую производные
        TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction firstDerivative = differentialOperator.derive(originalFunction);
        TabulatedFunction secondDerivative = differentialOperator.derive(firstDerivative);

        // Сериализация всех трех функций в файл
        try (FileOutputStream fileOutputStream = new FileOutputStream("output/serialized linked list functions.bin");
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

            FunctionsIO.serialize(bufferedOutputStream, originalFunction);
            FunctionsIO.serialize(bufferedOutputStream, firstDerivative);
            FunctionsIO.serialize(bufferedOutputStream, secondDerivative);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Десериализация функций из файла
        try (FileInputStream fileInputStream = new FileInputStream("output/serialized linked list functions.bin");
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            // Десериализуем первую функцию
            TabulatedFunction deserializedOriginal = FunctionsIO.deserialize(bufferedInputStream);
            // Десериализуем вторую функци
            TabulatedFunction deserializedFirstDerivative = FunctionsIO.deserialize(bufferedInputStream);
            // Десериализуем третью функцию
            TabulatedFunction deserializedSecondDerivative = FunctionsIO.deserialize(bufferedInputStream);

            System.out.println("\nИсходная функция:");
            System.out.println(deserializedOriginal.toString());

            System.out.println("\nПервая производная:");
            System.out.println(deserializedFirstDerivative.toString());

            System.out.println("\nВторая производная:");
            System.out.println(deserializedSecondDerivative.toString());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}