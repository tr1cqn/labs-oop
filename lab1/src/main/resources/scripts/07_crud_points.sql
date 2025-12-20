
--  операции для таблицы points

-- Найти все точки
SELECT id, func_id, x_value, y_value FROM points;

-- Найти точку по ID
SELECT id, func_id, x_value, y_value FROM points WHERE id = ?;

-- Найти все точки функции
SELECT id, func_id, x_value, y_value FROM points WHERE func_id = ?;

-- Найти точки функции, отсортированные по x_value
SELECT id, func_id, x_value, y_value FROM points 
WHERE func_id = ? 
ORDER BY x_value ASC;

-- Найти точку функции по x_value
SELECT id, func_id, x_value, y_value FROM points 
WHERE func_id = ? AND x_value = ?;

-- Найти точки функции в диапазоне x_value
SELECT id, func_id, x_value, y_value FROM points 
WHERE func_id = ? AND x_value BETWEEN ? AND ? 
ORDER BY x_value ASC;

-- Найти точки функции с определенным y_value
SELECT id, func_id, x_value, y_value FROM points 
WHERE func_id = ? AND y_value = ?;

-- Найти точки функции с y_value в диапазоне
SELECT id, func_id, x_value, y_value FROM points 
WHERE func_id = ? AND y_value BETWEEN ? AND ? 
ORDER BY x_value ASC;

-- Подсчитать количество точек функции
SELECT COUNT(*) as point_count FROM points WHERE func_id = ?;

-- Найти минимальное и максимальное x_value функции
SELECT MIN(x_value) as min_x, MAX(x_value) as max_x 
FROM points WHERE func_id = ?;

-- Добавить новую точку
INSERT INTO points (func_id, x_value, y_value) 
VALUES (?, ?, ?);

-- Добавить несколько точек одной функцией
INSERT INTO points (func_id, x_value, y_value) 
VALUES 
    (?, ?, ?),
    (?, ?, ?),
    (?, ?, ?);

-- Обновить y_value точки по ID
UPDATE points 
SET y_value = ? 
WHERE id = ?;

-- Обновить y_value точки функции по x_value
UPDATE points 
SET y_value = ? 
WHERE func_id = ? AND x_value = ?;

-- Обновить x_value и y_value точки
UPDATE points 
SET x_value = ?, y_value = ? 
WHERE id = ?;

-- Обновить все точки функции (умножить y_value на коэффициент)
UPDATE points 
SET y_value = y_value * ? 
WHERE func_id = ?;

-- Удалить точку по ID
DELETE FROM points WHERE id = ?;

-- Удалить точку функции по x_value
DELETE FROM points WHERE func_id = ? AND x_value = ?;

-- Удалить все точки функции
DELETE FROM points WHERE func_id = ?;

-- Удалить точки функции в диапазоне x_value
DELETE FROM points 
WHERE func_id = ? AND x_value BETWEEN ? AND ?;

