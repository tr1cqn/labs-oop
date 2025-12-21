# Инструкция по выполнению задания: Сравнение производительности

## Задание
Сравнить скорость обработки запросов в обоих случаях (на таблицах с минимумом 10к записей). Результаты записать в таблицу. Выгрузить таблицу на GitHub.

## Структура решения

### Framework ветка (текущая):
- `BenchmarkResult.java` - класс для хранения результатов
- `ExcelWriter.java` - класс для создания Excel файлов
- `FrameworkBenchmarkService.java` - сервис для выполнения бенчмарков
- `FrameworkBenchmarkRunner.java` - класс для запуска бенчмарков
- `PerformanceComparison.java` - класс для объединения результатов

### Manual ветка:
- `ManualBenchmarkServiceTemplate.java` - шаблон для создания ManualBenchmarkService
- Нужно создать `ManualBenchmarkService.java` используя DAO классы
- Нужно создать `ManualBenchmarkRunner.java` аналогично FrameworkBenchmarkRunner

---

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

---

### Шаг 2: Запуск бенчмарка Framework (текущая ветка)

1. Убедитесь, что вы в ветке `framework`:
   ```bash
   git branch --show-current
   ```

2. Проверьте настройки подключения в `EntityManagerProvider.java`:
   - URL: `jdbc:postgresql://localhost:5432/lab5_db`
   - User: `postgres`
   - Password: `postgres` (или ваш пароль)

3. Запустите бенчмарк:
   - Откройте `lab1/src/main/java/benchmark/FrameworkBenchmarkRunner.java`
   - Нажмите ▶️ Run (или Shift+F10)
   - Дождитесь завершения (генерация 10,000+ записей займет время)

4. Результаты:
   - В консоли появятся значения времени в миллисекундах
   - Файл `performance_framework.xlsx` будет создан автоматически

---

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

---

### Шаг 4: Создание бенчмарка для Manual ветки

1. Скопируйте `ManualBenchmarkServiceTemplate.java` и переименуйте в `ManualBenchmarkService.java`

2. Реализуйте методы аналогично `FrameworkBenchmarkService.java`, но используйте DAO классы:
   ```java
   // Вместо:
   userRepository.save(user);
   
   // Используйте:
   userDAO.insert(user.getLogin(), user.getPassword(), user.getEmail());
   ```

3. Создайте `ManualBenchmarkRunner.java`:
   ```java
   public class ManualBenchmarkRunner {
       public static void main(String[] args) {
           ManualBenchmarkService benchmarkService = new ManualBenchmarkService();
           List<BenchmarkResult> results = benchmarkService.runBenchmarks();
           ExcelWriter excelWriter = new ExcelWriter();
           excelWriter.saveToExcel(results, "performance_manual.xlsx");
       }
   }
   ```

4. Запустите бенчмарк:
   - Откройте `ManualBenchmarkRunner.java`
   - Нажмите ▶️ Run
   - Дождитесь завершения

5. Результаты:
   - Файл `performance_manual.xlsx` будет создан автоматически

---

### Шаг 5: Объединение результатов

1. После получения результатов из обеих веток, используйте `PerformanceComparison`:
   ```java
   // В любой ветке (лучше в framework)
   List<BenchmarkResult> manualResults = // загрузить из performance_manual.xlsx или сохранить
   List<BenchmarkResult> frameworkResults = // загрузить из performance_framework.xlsx или сохранить
   
   PerformanceComparison.createComparisonTable(manualResults, frameworkResults);
   ```

2. Или создайте скрипт для объединения результатов из обоих Excel файлов

3. Файл `performance_comparison.xlsx` будет создан с таблицей сравнения

---

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

---

## Структура Excel таблицы

Файл `performance_comparison.xlsx` будет содержать таблицу:

| Операция | Manual (JDBC), мс | Framework (Hibernate), мс | Разница, мс |
|----------|-------------------|---------------------------|-------------|
| insertInUsersTable | ... | ... | ... |
| findUserById (100 операций) | ... | ... | ... |
| findAllUsers | ... | ... | ... |
| updateUser (100 операций) | ... | ... | ... |
| deleteUserById (100 операций) | ... | ... | ... |
| ... | ... | ... | ... |

---

## Примечания

- Генерация 10,000+ записей может занять несколько минут
- Убедитесь, что база данных имеет достаточно места
- При ошибках подключения проверьте настройки в соответствующих классах
- Результаты могут отличаться в зависимости от нагрузки системы

