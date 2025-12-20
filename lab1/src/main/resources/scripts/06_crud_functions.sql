
-- операции для таблицы functions

-- Найти все функции
SELECT id, user_id, name, type FROM functions;

-- Найти функцию по ID
SELECT id, user_id, name, type FROM functions WHERE id = ?;

-- Найти все функции пользователя
SELECT id, user_id, name, type FROM functions WHERE user_id = ?;

-- Найти функции по типу
SELECT id, user_id, name, type FROM functions WHERE type = ?;

-- Найти функции по имени (точное совпадение)
SELECT id, user_id, name, type FROM functions WHERE name = ?;

-- Найти функции по частичному совпадению имени
SELECT id, user_id, name, type FROM functions WHERE name LIKE ?;

-- Найти функции пользователя определенного типа
SELECT id, user_id, name, type FROM functions 
WHERE user_id = ? AND type = ?;

-- Подсчитать количество функций пользователя
SELECT COUNT(*) as function_count FROM functions WHERE user_id = ?;

-- Добавить новую функцию
INSERT INTO functions (user_id, name, type) 
VALUES (?, ?, ?);

-- Обновить имя функции
UPDATE functions 
SET name = ? 
WHERE id = ?;

-- Обновить тип функции
UPDATE functions 
SET type = ? 
WHERE id = ?;

-- Обновить имя и тип функции
UPDATE functions 
SET name = ?, type = ? 
WHERE id = ?;

-- Обновить все функции пользователя 
UPDATE functions 
SET type = ? 
WHERE user_id = ?;

-- Удалить функцию по ID
DELETE FROM functions WHERE id = ?;

-- Удалить все функции пользователя
DELETE FROM functions WHERE user_id = ?;

-- Удалить функции определенного типа
DELETE FROM functions WHERE type = ?;

