
-- CRUD операции для таблицы users

-- Найти всех пользователей
SELECT id, login, email FROM users;

-- Найти пользователя по ID
SELECT id, login, email FROM users WHERE id = ?;

-- Найти пользователя по login
SELECT id, login, email FROM users WHERE login = ?;

-- Найти пользователя по email
SELECT id, login, email FROM users WHERE email = ?;

-- Поиск пользователей по частичному совпадению login
SELECT id, login, email FROM users WHERE login LIKE ?;

-- Добавить нового пользователя
INSERT INTO users (login, password, email) 
VALUES (?, ?, ?);

-- Добавить пользователя без email
INSERT INTO users (login, password) 
VALUES (?, ?);

-- Обновить пароль пользователя
UPDATE users 
SET password = ? 
WHERE id = ?;

-- Обновить email пользователя
UPDATE users 
SET email = ? 
WHERE id = ?;

-- Обновить login пользователя
UPDATE users 
SET login = ? 
WHERE id = ?;

-- Обновить все данные пользователя
UPDATE users 
SET login = ?, password = ?, email = ? 
WHERE id = ?;

-- Удалить пользователя по ID
DELETE FROM users WHERE id = ?;

-- Удалить пользователя по login
DELETE FROM users WHERE login = ?;


