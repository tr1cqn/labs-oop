# Сравнение производительности

| Операция | Manual (JDBC) | Framework (Hibernate) |
|----------|---------------|-----------------------|
| SELECT * FROM points | 35 ms | 77 ms |
| JOIN functions + points | 3 ms | 18 ms |
| INSERT point | 5 ms | 55 ms |
| UPDATE point | 4 ms | 49 ms |
| DELETE point | 0 ms | 24 ms |