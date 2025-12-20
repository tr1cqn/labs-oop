
--операции для таблицы result



SELECT id, result_id, result FROM result;

-- Найти результат по ID
SELECT id, result_id, result FROM result WHERE id = ?;

-- Найти все результаты функции
SELECT id, result_id, result FROM result WHERE result_id = ?;

-- Найти последний результат функции (по ID)
SELECT id, result_id, result FROM result 
WHERE result_id = ? 
ORDER BY id DESC 
LIMIT 1;

-- Найти результаты функции, отсортированные по ID
SELECT id, result_id, result FROM result 
WHERE result_id = ? 
ORDER BY id ASC;

-- Подсчитать количество результатов функции
SELECT COUNT(*) as result_count FROM result WHERE result_id = ?;

-- Найти результаты по частичному совпадению текста
SELECT id, result_id, result FROM result 
WHERE result LIKE ?;

-- Найти результаты функции по частичному совпадению текста
SELECT id, result_id, result FROM result 
WHERE result_id = ? AND result LIKE ?;


-- Добавить новый результат
INSERT INTO result (result_id, result) 
VALUES (?, ?);

-- Добавить несколько результатов одной функцией
INSERT INTO result (result_id, result) 
VALUES 
    (?, ?),
    (?, ?),
    (?, ?);


-- Обновить результат по ID
UPDATE result 
SET result = ? 
WHERE id = ?;

-- Обновить все результаты функции
UPDATE result 
SET result = ? 
WHERE result_id = ?;

-- Обновить результат функции 
UPDATE result 
SET result = ? 
WHERE result_id = ? 
AND id = (SELECT MAX(id) FROM result WHERE result_id = ?);


-- Удалить результат по ID
DELETE FROM result WHERE id = ?;

-- Удалить все результаты функции
DELETE FROM result WHERE result_id = ?;

-- Удалить последний результат функции
DELETE FROM result 
WHERE result_id = ? 
AND id = (SELECT MAX(id) FROM result WHERE result_id = ?);

-- Удалить результаты функции по частичному совпадению текста
DELETE FROM result 
WHERE result_id = ? AND result LIKE ?;

