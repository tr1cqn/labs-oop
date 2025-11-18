package io;

import functions.*;
import operations.*;

import java.io.*;

public class ArrayTabulatedFunctionSerialization {
    public static void main(String[] args) {

        double[] xValues = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] yValues = {0.0, 0.25, 1.0, 2.25, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);


        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction firstDerivative = operator.derive(function);
        TabulatedFunction secondDerivative = operator.derive(firstDerivative);

        // Сериализация
        try (FileOutputStream fos = new FileOutputStream("output/serialized array functions.bin");
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {


            FunctionsIO.serialize(bos, function);
            FunctionsIO.serialize(bos, firstDerivative);
            FunctionsIO.serialize(bos, secondDerivative);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Десериализация
        try (FileInputStream fis = new FileInputStream("output/serialized array functions.bin");
             BufferedInputStream bis = new BufferedInputStream(fis)) {


            TabulatedFunction deserializedFunction = FunctionsIO.deserialize(bis);
            TabulatedFunction deserializedFirstDerivative = FunctionsIO.deserialize(bis);
            TabulatedFunction deserializedSecondDerivative = FunctionsIO.deserialize(bis);


            System.out.println("Исходная функция:");
            System.out.println(deserializedFunction.toString());

            System.out.println("\nПервая производная:");
            System.out.println(deserializedFirstDerivative.toString());

            System.out.println("\nВторая производная:");
            System.out.println(deserializedSecondDerivative.toString());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}