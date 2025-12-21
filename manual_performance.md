# Результаты производительности (Manual JDBC)

**Тестовые данные:** 10,000+ записей в таблице points

| Операция | Время (ms) |
|----------|------------|
| SELECT * FROM points | 35 |
| JOIN functions + points | 3 |
| INSERT point | 5 |
| UPDATE point | 4 |
| DELETE point | 0 |
