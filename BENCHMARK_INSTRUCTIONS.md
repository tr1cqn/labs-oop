# Инструкция по выполнению задания: Сравнение производительности

## Задание
Сравнить скорость обработки запросов в обоих случаях (на таблицах с минимумом 10к записей). Результаты записать в таблицу. Выгрузить таблицу на GitHub.

## Пошаговая инструкция

### Шаг 1: Подготовка базы данных

1. Убедитесь, что PostgreSQL запущен
2. Создайте базу данных:
   ```sql
   CREATE DATABASE lab5_db;
   ```
3. Выполните SQL-скрипты из `lab1/src/main/resources/scripts/`:
   - `01_create_users.sql`
   - `02_create_functions.sql`
   - `03_create_points.sql`
   - `04_create_result.sql`

### Шаг 2: Запуск бенчмарка в ветке Framework

1. Убедитесь, что вы в ветке `framework`:
   ```bash
   git branch --show-current
   ```

2. Проверьте настройки подключения в `lab1/src/main/java/repository/impl/EntityManagerProvider.java`:
   - URL: `jdbc:postgresql://localhost:5432/lab5_db`
   - User: `postgres`
   - Password: `postgres`
   - При необходимости измените параметры

3. Запустите бенчмарк:
   - Откройте `lab1/src/main/java/benchmark/PerformanceTestRunner.java`
   - Нажмите ▶️ Run (или Shift+F10)
   - Дождитесь завершения (генерация 10,000+ записей займет время)

4. Результаты:
   - В консоли появятся значения времени в миллисекундах
   - Файл `performance_framework.xlsx` будет создан автоматически
   - Файл `performance_comparison.xlsx` будет обновлен с результатами Framework

### Шаг 3: Переключение на ветку Manual

1. Переключитесь на ветку `manual`:
   ```bash
   git checkout manual
   ```

2. Убедитесь, что в ветке manual есть DAO классы:
   - `database/ConnectionManager.java`
   - `database/dao/UserDAO.java`
   - `database/dao/FunctionDAO.java`
   - `database/dao/PointDAO.java`
   - `database/dao/ResultDAO.java`

### Шаг 4: Создание и запуск бенчмарка в ветке Manual

1. Создайте файл `lab1/src/main/java/benchmark/ManualPerformanceBenchmark.java`:
   - Используйте структуру из `FrameworkPerformanceBenchmark.java`
   - Замените Repository на DAO классы
   - Используйте методы DAO вместо методов Repository

2. Создайте файл `lab1/src/main/java/benchmark/ManualPerformanceTestRunner.java`:
   - Аналогично `PerformanceTestRunner.java`
   - Используйте `ManualPerformanceBenchmark` вместо `FrameworkPerformanceBenchmark`

3. Запустите бенчмарк:
   - Откройте `ManualPerformanceTestRunner.java`
   - Нажмите ▶️ Run
   - Дождитесь завершения

4. Результаты:
   - В консоли появятся значения времени
   - Файл `performance_comparison.xlsx` будет обновлен с результатами Manual и разницей

### Шаг 5: Проверка результатов

1. Откройте файл `performance_comparison.xlsx` в Excel или другом табличном редакторе
2. Убедитесь, что все колонки заполнены:
   - Manual (JDBC), мс
   - Framework (Hibernate), мс
   - Разница, мс

### Шаг 6: Выгрузка на GitHub

1. Добавьте файл в git:
   ```bash
   git add performance_comparison.xlsx
   ```

2. Закоммитьте:
   ```bash
   git commit -m "feat: результаты сравнения производительности Manual vs Framework"
   ```

3. Отправьте на GitHub:
   ```bash
   git push
   ```

## Структура результатов

Файл `performance_comparison.xlsx` будет содержать таблицу:

| Операция | Manual (JDBC), мс | Framework (Hibernate), мс | Разница, мс |
|----------|-------------------|---------------------------|-------------|
| Поиск по ID (100 операций) | ... | ... | ... |
| Поиск всех записей | ... | ... | ... |
| Поиск с условиями (50 операций) | ... | ... | ... |
| Вставка (100 записей) | ... | ... | ... |
| Обновление (100 записей) | ... | ... | ... |
| Удаление (100 записей) | ... | ... | ... |

## Примечания

- Генерация 10,000+ записей может занять несколько минут
- Убедитесь, что база данных имеет достаточно места
- При ошибках подключения проверьте настройки в соответствующих классах (EntityManagerProvider для Framework, ConnectionManager для Manual)

