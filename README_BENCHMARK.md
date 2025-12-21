# Бенчмарки производительности Manual vs Framework

## Быстрый старт

### Framework ветка (текущая):

1. Запустите `FrameworkBenchmarkRunner.java`
2. Результаты сохранятся в `performance_framework.xlsx`

### Manual ветка:

1. Переключитесь: `git checkout manual`
2. Создайте `ManualBenchmarkService.java` на основе `ManualBenchmarkServiceTemplate.java`
3. Создайте `ManualBenchmarkRunner.java`
4. Запустите бенчмарк
5. Результаты сохранятся в `performance_manual.xlsx`

### Объединение результатов:

Используйте `PerformanceComparison.createComparisonTable()` для создания итоговой таблицы `performance_comparison.xlsx`

Подробная инструкция в файле `BENCHMARK_INSTRUCTIONS.md`

