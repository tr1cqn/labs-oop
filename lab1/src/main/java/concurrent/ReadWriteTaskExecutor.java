package concurrent;

import functions.*;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;

public class ReadWriteTaskExecutor {
    public static void main(String[] args) {
        MathFunction sourceFunction = new ConstantFunction(-1.0);
        TabulatedFunction function = new LinkedListTabulatedFunction(sourceFunction, 1, 100, 100);

        Object monitor = new Object();

        ReadTask readTask = new ReadTask(function, monitor);
        WriteTask writeTask = new WriteTask(function, 0.5, monitor);

        Thread readThread = new Thread(readTask);
        Thread writeThread = new Thread(writeTask);

        readThread.start();
        writeThread.start();

        try {
            readThread.join();
            writeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Программа завершена");
    }
}