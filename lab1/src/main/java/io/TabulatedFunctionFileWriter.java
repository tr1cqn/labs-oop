package io;

import functions.TabulatedFunction;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;

import java.io.*;

public class TabulatedFunctionFileWriter {
    public static void main(String[] args) {
        // Создаем тестовые функции
        double[] xValues = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] yValues = {0.0, 0.25, 1.0, 2.25, 4.0};

        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);

        try (
                // Создаем потоки для записи в текстовые файлы
                FileWriter arrayWriter = new FileWriter("output/array function.txt");
                FileWriter linkedListWriter = new FileWriter("output/linked list function.txt");
                BufferedWriter arrayBufferedWriter = new BufferedWriter(arrayWriter);
                BufferedWriter linkedListBufferedWriter = new BufferedWriter(linkedListWriter)
        ) {
            // Записываем функции в соответствующие файлы
            FunctionsIO.writeTabulatedFunction(arrayBufferedWriter, arrayFunction);
            FunctionsIO.writeTabulatedFunction(linkedListBufferedWriter, linkedListFunction);

            System.out.println("Функции успешно записаны в текстовые файлы");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}