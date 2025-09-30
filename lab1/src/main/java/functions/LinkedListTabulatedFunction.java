package functions;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable {

    // Вспомогательный класс узла списка
    private static class Node {
        public double x;
        public double y;
        public Node next;
        public Node prev;
    }

    private Node head;

    // Конструктор 1 из массивов
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Массивы должны быть одинаковой длины");
        }
        if (xValues.length < 2) {
            throw new IllegalArgumentException("Необходимо минимум 2 точки");
        }

        // Проверяем упорядоченность xValues
        for (int i = 1; i < xValues.length; i++) {
            if (xValues[i] <= xValues[i - 1]) {
                throw new IllegalArgumentException("Значения x должны быть строго возрастающими");
            }
        }

        //  count через добавление узлов
        this.count = 0;

        // Добавляем узлы через цикл
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    // Конструктор 2 из функции
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не меньше 2");
        }

        this.count = 0;

        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        if (xFrom == xTo) {
            // Все точки одинаковые
            double y = source.apply(xFrom);
            for (int i = 0; i < count; i++) {
                addNode(xFrom, y);
            }
        } else {
            // Равномерная дискретизация
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; i++) {
                double x = xFrom + i * step;
                double y = source.apply(x);
                addNode(x, y);
            }
        }
    }

    @Override
    public int getCount() {
        return count;
    }

    // Приватный метод добавления узла в конец списка
    private void addNode(double x, double y) {
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;

        if (head == null) {
            // Список пустой новый узел становится головой
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
    }

    // Вспомогательный метод получения узла по индексу
    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Индекс: " + index + ", Размер: " + count);
        }

        //  если индекс в первой половине идем с головы
        if (index <= count / 2) {
            Node current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current;
        } else {
            // Если индекс во второй половине идем с хвоста
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
        if (head == null) {
            throw new IllegalStateException("Функция пуста");
        }

        // Если x меньше всех значений
        if (x < head.x) {
            return 0;
        }

        // Если x больше или равен последнему значению
        if (x >= head.prev.x) {
            return count;
        }

        // Ищем интервал, в который попадает x
        Node current = head;
        for (int i = 0; i < count - 1; i++) {
            if (x >= current.x && x < current.next.x) {
                return i;
            }
            current = current.next;
        }

        return count;
    }

    @Override
    protected double extrapolateLeft(double x) {
        if (count == 1) {
            return head.y;
        }
        Node left = head;
        Node right = head.next;
        return interpolate(x, left.x, right.x, left.y, right.y);
    }

    @Override
    protected double extrapolateRight(double x) {
        if (count == 1) {
            return head.y;
        }
        Node left = head.prev.prev;
        Node right = head.prev;
        return interpolate(x, left.x, right.x, left.y, right.y);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (count == 1) {
            return head.y;
        }
        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;
        return interpolate(x, leftNode.x, rightNode.x, leftNode.y, rightNode.y);
    }
    public void remove(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Индекс: " + index + ", Размер: " + count);
        }
        // Метод getNode(index) находит узел по индексу в списке
        Node toRemove = getNode(index);

        if (count == 1) {
            // Просто обнуляем голову списка список становится пустым
            head = null;
        } else {
            //  У узла toRemove.prev устанавливаем next на toRemove.next
            toRemove.prev.next = toRemove.next;

            // узла toRemove.next устанавливаем prev на toRemove.prev
            toRemove.next.prev = toRemove.prev;

            // Проверяем, не является ли удаляемый узел головой списка
            if (toRemove == head) {
                // Если удаляем голову, то новая голова - следующий узел
                head = head.next;
            }
        }
        count--;
    }
        // Реализация метода insert из интерфейса Insertable
    @Override
    public void insert(double x, double y) {
        // Если список пустой
        if (head == null) {
            addNode(x, y);
            return;
        }

        // Ищем узел с таким x или место для вставки
        Node current = head;
        
        do {
            // Если нашли существующий x - заменяем y
            if (Math.abs(current.x - x) < 1e-10) {
                current.y = y;
                return;
            }
            
            // Если нашли место для вставки (текущий x больше вставляемого)
            if (current.x > x) {
                break;
            }
            
            current = current.next;
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
        if (current == head) {
            head = newNode;
        }

        count++;
    }
}
