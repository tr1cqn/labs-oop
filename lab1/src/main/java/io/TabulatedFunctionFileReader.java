package io;

import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;

import java.io.*;

public class TabulatedFunctionFileReader {
    public static void main(String[] args) {
        try (FileReader arrayFileReader = new FileReader("input/function.txt");
             FileReader linkedListFileReader = new FileReader("input/function.txt");
             BufferedReader arrayBufferedReader = new BufferedReader(arrayFileReader);
             BufferedReader linkedListBufferedReader = new BufferedReader(linkedListFileReader)) {

            // Создаем фабрики
            TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
            TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();

            // Читаем функции
            TabulatedFunction arrayFunction = FunctionsIO.readTabulatedFunction(arrayBufferedReader, arrayFactory);
            TabulatedFunction linkedListFunction = FunctionsIO.readTabulatedFunction(linkedListBufferedReader, linkedListFactory);

            // Выводим результаты
            System.out.println("Array function:");
            System.out.println(arrayFunction);
            System.out.println("\nLinked list function:");
            System.out.println(linkedListFunction);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}