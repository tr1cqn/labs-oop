package io;

import functions.*;
import functions.factory.*;
import operations.TabulatedDifferentialOperator;
import java.io.*;


public class TabulatedFunctionFileInputStream {
    public static void main(String[] args) {

        // Чтение из бинарного файла
        try (FileInputStream fileInputStream = new FileInputStream("input/binary function.bin");
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            // Создаем Array функцию
            TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
            TabulatedFunction arrayFunction = FunctionsIO.readTabulatedFunction(bufferedInputStream, arrayFactory);

            System.out.println(arrayFunction.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Чтение из консоли
        System.out.println("Введите размер и значения функции");

        try {
            // Создаем LinkedList функцию
            TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
            TabulatedFunction consoleFunction = FunctionsIO.readTabulatedFunction(
                    new BufferedInputStream(System.in), linkedListFactory);

            System.out.println(consoleFunction.toString());

            // Вычисление и вывод производной
            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
            TabulatedFunction derivative = differentialOperator.derive(consoleFunction);
            System.out.println(derivative.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
