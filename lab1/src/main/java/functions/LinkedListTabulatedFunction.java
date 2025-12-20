package functions;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.Serializable;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable {

    private static final long serialVersionUID = 337603668939094478L;
    private static final Logger logger = LogManager.getLogger(LinkedListTabulatedFunction.class);

    private static class Node implements Serializable {
        private static final long serialVersionUID = 1L;
        public double x;
        public double y;
        public Node next;
        public Node prev;
    }

    private Node head;


    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        logger.info("Создание LinkedListTabulatedFunction из массивов, размер: {}", xValues.length);
        //  Длина должна быть ≥ 2 точек
        if (xValues.length < 2) {
            logger.error("Попытка создать функцию с менее чем 2 точками: {}", xValues.length);
            throw new IllegalArgumentException("Длина таблицы должна быть не менее 2 точек");
        }

        //  Одинаковая длина массивов
        checkLengthIsTheSame(xValues, yValues);
        checkSorted(xValues);
        this.count = 0;

        // Добавляем узлы через цикл
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
        logger.info("LinkedListTabulatedFunction успешно создана, количество точек: {}, границы: [{}, {}]", 
            count, xValues[0], xValues[xValues.length - 1]);
    }

    // Конструктор 2 из функции
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        logger.info("Создание LinkedListTabulatedFunction из функции {}, диапазон: [{}, {}], количество точек: {}", 
            source.getClass().getSimpleName(), xFrom, xTo, count);

        if (count < 2) {
            logger.error("Попытка создать функцию с менее чем 2 точками: {}", count);
            throw new IllegalArgumentException("Минимум 2 точки требуется");
        }

        this.count = 0;

        if (xFrom > xTo) {
            logger.debug("Границы переставлены местами: xFrom={}, xTo={}", xFrom, xTo);
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        if (xFrom == xTo) {
            // Все точки одинаковые
            logger.debug("Границы совпадают, заполнение константным значением");
            double y = source.apply(xFrom);
            for (int i = 0; i < count; i++) {
                addNode(xFrom, y);
            }
        } else {
            double step = (xTo - xFrom) / (count - 1);
            logger.debug("Шаг табуляции: {}", step);
            for (int i = 0; i < count; i++) {
                double x = xFrom + i * step;
                double y = source.apply(x);
                addNode(x, y);
            }
        }
        logger.info("LinkedListTabulatedFunction успешно создана из функции, количество точек: {}", count);
    }

    @Override
    public int getCount() {
        return count;
    }

    // Приватный метод добавления узла в конец списка
    private void addNode(double x, double y) {
        logger.trace("Добавление узла ({}, {}) в связанный список", x, y);
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;

        if (head == null) {
            // Список пустой новый узел становится головой
            logger.trace("Список пустой, новый узел становится головой");
            head = newNode;
            head.next = head;
            head.prev = head;
        } else {
            // Список не пустой добавляем в конец
            Node last = head.prev;

            // Связываем последний узел с новым
            last.next = newNode;
            newNode.prev = last;

            // Связываем новый узел с головой
            newNode.next = head;
            head.prev = newNode;
        }
        count++;
        logger.trace("Узел добавлен, текущий размер списка: {}", count);
    }

    // Вспомогательный метод получения узла по индексу
    private Node getNode(int index) {
        logger.trace("Получение узла по индексу: {}", index);
        if (index < 0 || index >= count) {
            logger.error("Попытка получить узел с недопустимым индексом: {}, размер: {}", index, count);
            throw new IndexOutOfBoundsException("Индекс: " + index + ", Размер: " + count);
        }

        //  если индекс в первой половине идем с головы
        if (index <= count / 2) {
            logger.trace("Индекс в первой половине, поиск с головы");
            Node current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current;
        } else {
            // Если индекс во второй половине идем с хвоста
            logger.trace("Индекс во второй половине, поиск с хвоста");
            Node current = head.prev;
            for (int i = count - 1; i > index; i--) {
                current = current.prev;
            }
            return current;
        }
    }

    // Реализация методов TabulatedFunction
    @Override
    public double getX(int index) {
        return getNode(index).x;
    }

    @Override
    public double getY(int index) {
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value) {
        getNode(index).y = value;
    }

    @Override
    public int indexOfX(double x) {
        if (head == null) return -1;

        Node current = head;
        for (int i = 0; i < count; i++) {
            if (Math.abs(current.x - x) < 1e-10) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        if (head == null) return -1;

        Node current = head;
        for (int i = 0; i < count; i++) {
            if (Math.abs(current.y - y) < 1e-10) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public double leftBound() {
        if (head == null) {
            throw new IllegalStateException("Функция пуста");
        }
        return head.x;
    }

    @Override
    public double rightBound() {
        if (head == null) {
            throw new IllegalStateException("Функция пуста");
        }
        return head.prev.x;
    }

    // Реализация абстрактных методов

    @Override
    protected int floorIndexOfX(double x) {
        logger.debug("Поиск floorIndex для x={} в LinkedListTabulatedFunction", x);
        if (head == null) {
            logger.error("Попытка найти floorIndex в пустой функции");
            throw new IllegalStateException("Функция пуста");
        }
        if (x < head.x) {
            logger.error("x={} меньше левой границы {}", x, head.x);
            throw new IllegalArgumentException("x меньше левой границы: " + x + " < " + head.x);
        }

        // Если x больше или равен последнему значению
        if (x >= head.prev.x) {
            logger.debug("x={} больше или равен правой границе {}, возвращается count={}", x, head.prev.x, count);
            return count;
        }

        // Ищем интервал, в который попадает x
        Node current = head;
        for (int i = 0; i < count - 1; i++) {
            if (x >= current.x && x < current.next.x) {
                logger.debug("Найден floorIndex={} для x={} (интервал: [{}, {}))", i, x, current.x, current.next.x);
                return i;
            }
            current = current.next;
        }

        logger.debug("Возвращается count={}", count);
        return count;
    }

    @Override
    protected double extrapolateLeft(double x) {
        Node left = head;
        Node right = head.next;
        return interpolate(x, left.x, right.x, left.y, right.y);
    }

    @Override
    protected double extrapolateRight(double x) {
        Node left = head.prev.prev;
        Node right = head.prev;
        return interpolate(x, left.x, right.x, left.y, right.y);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;
        return interpolate(x, leftNode.x, rightNode.x, leftNode.y, rightNode.y);
    }
    public void remove(int index) {
        logger.info("Удаление точки с индексом {} из LinkedListTabulatedFunction", index);
        if (index < 0 || index >= count) {
            logger.error("Попытка удалить точку с недопустимым индексом: {}, размер функции: {}", index, count);
            throw new IndexOutOfBoundsException("Индекс: " + index + ", Размер: " + count);
        }
        Node toRemove = getNode(index);
        logger.debug("Удаление узла ({}, {}), текущий размер: {}", toRemove.x, toRemove.y, count);

        if (count == 1) {
            logger.debug("Удаление последнего узла, список становится пустым");
            head = null;
        } else {
            toRemove.prev.next = toRemove.next;
            toRemove.next.prev = toRemove.prev;
            if (toRemove == head) {
                // Если удаляем голову, то новая голова - следующий узел
                logger.debug("Удаляется голова списка, новая голова - следующий узел");
                head = head.next;
            }
        }
        count--;
        logger.info("Точка успешно удалена, новый размер: {}", count);
    }
        @Override
        public void insert(double x, double y) {
            logger.info("Вставка точки ({}, {}) в LinkedListTabulatedFunction", x, y);
            if (head == null) {
                logger.debug("Список пустой, добавление первого узла");
                addNode(x, y);
                return;
            }

            Node current = head;
            boolean found = false;
            int iterations = 0;

            do {
                if (Math.abs(current.x - x) < 1e-10) {
                    logger.debug("Точка с x={} уже существует, обновление значения y с {} на {}", 
                        x, current.y, y);
                    current.y = y;
                    return;
                }

                // Если нашли место для вставки (текущий x больше вставляемого)
                if (current.x > x) {
                    break;
                }

                current = current.next;
                iterations++;

                // Защита от бесконечного цикла
                if (iterations > count) {
                    logger.warn("Превышено количество итераций при поиске места вставки: {}", iterations);
                    break;
                }
            } while (current != head);

            // Создаем новый узел
            Node newNode = new Node();
            newNode.x = x;
            newNode.y = y;

            // Вставляем перед current
            Node prev = current.prev;

            newNode.prev = prev;
            newNode.next = current;
            prev.next = newNode;
            current.prev = newNode;

            // Если вставляем в начало, обновляем head
            if (current == head && x < head.x) {
                logger.debug("Вставка в начало списка, обновление головы");
                head = newNode;
            }

            count++;
            logger.info("Точка ({}, {}) успешно вставлена, новый размер: {}", x, y, count);
        }
    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private Node node = head;
            private int returnedCount = 0;

            @Override
            public boolean hasNext() {
                return returnedCount < count;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("Нет больше элементов в списке");
                }

                Point point = new Point(node.x, node.y);
                node = node.next;
                returnedCount++;

                return point;
            }
        };
    }

}
