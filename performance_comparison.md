# Сравнение производительности Manual (JDBC) vs Framework (Hibernate)

**Результаты сохраняются в Excel файл: `performance_comparison.xlsx`**

## Инструкция по выполнению

Подробная инструкция находится в файле `BENCHMARK_INSTRUCTIONS.md`

### Краткая инструкция:

1. **Framework ветка:**
   - Запустить `benchmark.PerformanceTestRunner`
   - Результаты автоматически сохранятся в `performance_comparison.xlsx`

2. **Manual ветка:**
   - Создать `ManualPerformanceBenchmark.java` используя DAO классы
   - Запустить бенчмарк
   - Результаты автоматически обновят `performance_comparison.xlsx`

3. **Выгрузка на GitHub:**
   ```bash
   git add performance_comparison.xlsx
   git commit -m "feat: результаты сравнения производительности Manual vs Framework"
   git push
   ```

## Структура Excel таблицы

| Операция | Manual (JDBC), мс | Framework (Hibernate), мс | Разница, мс |
|----------|-------------------|---------------------------|-------------|
| Поиск по ID (100 операций) | - | - | - |
| Поиск всех записей | - | - | - |
| Поиск с условиями (50 операций) | - | - | - |
| Вставка (100 записей) | - | - | - |
| Обновление (100 записей) | - | - | - |
| Удаление (100 записей) | - | - | - |
