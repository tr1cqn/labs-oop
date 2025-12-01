package concurrent;

import functions.*;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import java.util.ArrayList;
import java.util.List;


public class MultiplyingTaskExecutor {

    public static void main(String[] args) {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();


        MathFunction sourceFunction = new UnitFunction();
        TabulatedFunction function = new LinkedListTabulatedFunction(sourceFunction, 1, 100, 10);

        System.out.println("Исходная функция (первые 5 значений):");
        for (int i = 0; i < Math.min(5, function.getCount()); i++) {
            System.out.printf("x = %.1f, y = %.1f%n", function.getX(i), function.getY(i));
        }

        // Создание списка потоков
        List<Thread> threads = new ArrayList<>();

        // Создание 10 задач MultiplyingTask и потоков для них
        for (int i = 0; i < 10; i++) {
            MultiplyingTask task = new MultiplyingTask(function);
            Thread thread = new Thread(task, "Multiplier-" + (i + 1));
            threads.add(thread);
        }

        // Старт всех потоков
        for (Thread thread : threads) {
            thread.start();
        }

        // Усыпление текущего потока на 2 секунды
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Вывод табулированной функции после работы потоков
        System.out.println("\nФункция после выполнения потоков:");
        for (int i = 0; i < function.getCount(); i++) {
            System.out.printf("x = %.1f, y = %.1f%n", function.getX(i), function.getY(i));
        }

        System.out.println("\nАнализ:");
        System.out.println("Теоретически каждое y должно быть равно 1 * 2^10 = 1024");
        System.out.println("Фактически полученные значения могут отличаться из-за гонки данных.");
    }
}
