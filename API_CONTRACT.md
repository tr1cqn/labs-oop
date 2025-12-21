# Единый унифицированный контракт API

## Версия: 1.0
## Дата создания: 2024
## Использование: Лабораторная работа №6 и №7

---

## Общие принципы

### Базовый URL
- **Base URL**: `/api/v1`
- **Формат данных**: JSON
- **Кодировка**: UTF-8
- **Аутентификация**: Basic Auth (будет добавлена в ЛР6)

### Структура ответов

#### Успешный ответ
```json
{
  "success": true,
  "data": { ... },
  "message": "Операция выполнена успешно"
}
```

#### Ответ с ошибкой
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Описание ошибки"
  }
}
```

#### HTTP статусы
- `200 OK` - успешная операция
- `201 Created` - ресурс создан
- `400 Bad Request` - неверный запрос
- `401 Unauthorized` - требуется аутентификация
- `403 Forbidden` - недостаточно прав
- `404 Not Found` - ресурс не найден
- `500 Internal Server Error` - ошибка сервера

---

## 1. API для работы с Users

### GET `/api/v1/users`
Получить всех пользователей

**Параметры запроса:**
- `sortBy` (optional) - поле для сортировки (id, login, email)
- `order` (optional) - направление сортировки (asc, desc)
- `limit` (optional) - ограничение количества результатов
- `offset` (optional) - смещение для пагинации

**Ответ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "login": "user1",
      "email": "user1@example.com"
    }
  ],
  "total": 10
}
```

### GET `/api/v1/users/{id}`
Получить пользователя по ID

**Параметры пути:**
- `id` (required) - ID пользователя

**Ответ:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "login": "user1",
    "email": "user1@example.com"
  }
}
```

### GET `/api/v1/users/search?login={login}`
Поиск пользователя по login (точное совпадение)

**Параметры запроса:**
- `login` (required) - логин пользователя

### GET `/api/v1/users/search?email={email}`
Поиск пользователя по email (точное совпадение)

**Параметры запроса:**
- `email` (required) - email пользователя

### GET `/api/v1/users/search?loginLike={pattern}`
Поиск пользователей по частичному совпадению login

**Параметры запроса:**
- `loginLike` (required) - паттерн для поиска

### POST `/api/v1/users`
Создать пользователя

**Тело запроса:**
```json
{
  "login": "newuser",
  "password": "password123",
  "email": "newuser@example.com"
}
```

**Ответ:**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "login": "newuser",
    "email": "newuser@example.com"
  },
  "message": "Пользователь успешно создан"
}
```

### PUT `/api/v1/users/{id}`
Обновить все данные пользователя

**Тело запроса:**
```json
{
  "login": "updateduser",
  "password": "newpassword",
  "email": "updated@example.com"
}
```

### PATCH `/api/v1/users/{id}/password`
Обновить пароль пользователя

**Тело запроса:**
```json
{
  "password": "newpassword"
}
```

### PATCH `/api/v1/users/{id}/email`
Обновить email пользователя

**Тело запроса:**
```json
{
  "email": "newemail@example.com"
}
```

### PATCH `/api/v1/users/{id}/login`
Обновить login пользователя

**Тело запроса:**
```json
{
  "login": "newlogin"
}
```

### DELETE `/api/v1/users/{id}`
Удалить пользователя по ID

**Ответ:**
```json
{
  "success": true,
  "message": "Пользователь успешно удален"
}
```

### DELETE `/api/v1/users?login={login}`
Удалить пользователя по login

**Параметры запроса:**
- `login` (required) - логин пользователя

---

## 2. API для работы с Functions

### GET `/api/v1/functions`
Получить все функции

**Параметры запроса:**
- `sortBy` (optional) - поле для сортировки (id, name, type, userId)
- `order` (optional) - направление сортировки (asc, desc)
- `limit` (optional) - ограничение количества результатов
- `offset` (optional) - смещение для пагинации

### GET `/api/v1/functions/{id}`
Получить функцию по ID

### GET `/api/v1/functions/user/{userId}`
Получить все функции пользователя

**Параметры пути:**
- `userId` (required) - ID пользователя

### GET `/api/v1/functions/search?type={type}`
Поиск функций по типу

**Параметры запроса:**
- `type` (required) - тип функции

### GET `/api/v1/functions/search?name={name}`
Поиск функций по имени (точное совпадение)

**Параметры запроса:**
- `name` (required) - имя функции

### GET `/api/v1/functions/search?nameLike={pattern}`
Поиск функций по частичному совпадению имени

**Параметры запроса:**
- `nameLike` (required) - паттерн для поиска

### GET `/api/v1/functions/search?userId={userId}&type={type}`
Поиск функций пользователя определенного типа

**Параметры запроса:**
- `userId` (required) - ID пользователя
- `type` (required) - тип функции

### GET `/api/v1/functions/user/{userId}/count`
Подсчитать количество функций пользователя

**Ответ:**
```json
{
  "success": true,
  "data": {
    "function_count": 5
  }
}
```

### POST `/api/v1/functions`
Создать функцию

**Тело запроса:**
```json
{
  "userId": 1,
  "name": "Linear Function",
  "type": "linear"
}
```

**Ответ:**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "userId": 1,
    "name": "Linear Function",
    "type": "linear"
  },
  "message": "Функция успешно создана"
}
```

### PUT `/api/v1/functions/{id}`
Обновить функцию

**Тело запроса:**
```json
{
  "name": "Updated Function",
  "type": "quadratic"
}
```

### PATCH `/api/v1/functions/{id}/name`
Обновить имя функции

**Тело запроса:**
```json
{
  "name": "New Function Name"
}
```

### PATCH `/api/v1/functions/{id}/type`
Обновить тип функции

**Тело запроса:**
```json
{
  "type": "polynomial"
}
```

### PATCH `/api/v1/functions/user/{userId}/type`
Обновить тип всех функций пользователя

**Тело запроса:**
```json
{
  "type": "newtype"
}
```

### DELETE `/api/v1/functions/{id}`
Удалить функцию по ID

### DELETE `/api/v1/functions/user/{userId}`
Удалить все функции пользователя

### DELETE `/api/v1/functions?type={type}`
Удалить функции определенного типа

**Параметры запроса:**
- `type` (required) - тип функции

---

## 3. API для работы с Points

### GET `/api/v1/points`
Получить все точки

**Параметры запроса:**
- `sortBy` (optional) - поле для сортировки (id, xValue, yValue, funcId)
- `order` (optional) - направление сортировки (asc, desc)

### GET `/api/v1/points/{id}`
Получить точку по ID

### GET `/api/v1/points/function/{funcId}`
Получить все точки функции

**Параметры пути:**
- `funcId` (required) - ID функции

**Параметры запроса:**
- `sortBy` (optional) - поле для сортировки (xValue, yValue, id)
- `order` (optional) - направление сортировки (asc, desc)

### GET `/api/v1/points/function/{funcId}/x/{xValue}`
Получить точку функции по x_value

**Параметры пути:**
- `funcId` (required) - ID функции
- `xValue` (required) - значение x

### GET `/api/v1/points/function/{funcId}/range?xMin={min}&xMax={max}`
Получить точки функции в диапазоне x_value

**Параметры запроса:**
- `xMin` (required) - минимальное значение x
- `xMax` (required) - максимальное значение x

### GET `/api/v1/points/function/{funcId}/y/{yValue}`
Получить точки функции с определенным y_value

**Параметры пути:**
- `funcId` (required) - ID функции
- `yValue` (required) - значение y

### GET `/api/v1/points/function/{funcId}/yRange?yMin={min}&yMax={max}`
Получить точки функции с y_value в диапазоне

**Параметры запроса:**
- `yMin` (required) - минимальное значение y
- `yMax` (required) - максимальное значение y

### GET `/api/v1/points/function/{funcId}/count`
Подсчитать количество точек функции

**Ответ:**
```json
{
  "success": true,
  "data": {
    "point_count": 100
  }
}
```

### GET `/api/v1/points/function/{funcId}/bounds`
Получить минимальное и максимальное x_value функции

**Ответ:**
```json
{
  "success": true,
  "data": {
    "min_x": 0.0,
    "max_x": 10.0
  }
}
```

### POST `/api/v1/points`
Создать точку

**Тело запроса:**
```json
{
  "funcId": 1,
  "xValue": 5.0,
  "yValue": 25.0
}
```

**Ответ:**
```json
{
  "success": true,
  "data": {
    "id": 50,
    "funcId": 1,
    "xValue": 5.0,
    "yValue": 25.0
  },
  "message": "Точка успешно создана"
}
```

### POST `/api/v1/points/batch`
Создать несколько точек

**Тело запроса:**
```json
{
  "funcId": 1,
  "points": [
    {"xValue": 1.0, "yValue": 10.0},
    {"xValue": 2.0, "yValue": 20.0},
    {"xValue": 3.0, "yValue": 30.0}
  ]
}
```

**Ответ:**
```json
{
  "success": true,
  "data": {
    "created": 3,
    "points": [
      {"id": 51, "funcId": 1, "xValue": 1.0, "yValue": 10.0},
      {"id": 52, "funcId": 1, "xValue": 2.0, "yValue": 20.0},
      {"id": 53, "funcId": 1, "xValue": 3.0, "yValue": 30.0}
    ]
  },
  "message": "Создано точек: 3"
}
```

### PUT `/api/v1/points/{id}`
Обновить точку

**Тело запроса:**
```json
{
  "xValue": 5.5,
  "yValue": 27.5
}
```

### PATCH `/api/v1/points/{id}/y`
Обновить y_value точки

**Тело запроса:**
```json
{
  "yValue": 30.0
}
```

### PATCH `/api/v1/points/function/{funcId}/x/{xValue}/y`
Обновить y_value точки функции по x_value

**Тело запроса:**
```json
{
  "yValue": 35.0
}
```

### PATCH `/api/v1/points/function/{funcId}/multiply?coefficient={coeff}`
Умножить y_value всех точек функции на коэффициент

**Параметры запроса:**
- `coefficient` (required) - коэффициент умножения

**Ответ:**
```json
{
  "success": true,
  "data": {
    "updated": 100
  },
  "message": "Обновлено точек: 100"
}
```

### DELETE `/api/v1/points/{id}`
Удалить точку по ID

### DELETE `/api/v1/points/function/{funcId}/x/{xValue}`
Удалить точку функции по x_value

### DELETE `/api/v1/points/function/{funcId}`
Удалить все точки функции

### DELETE `/api/v1/points/function/{funcId}/range?xMin={min}&xMax={max}`
Удалить точки функции в диапазоне x_value

---

## 4. API для работы с Results

### GET `/api/v1/results`
Получить все результаты

**Параметры запроса:**
- `sortBy` (optional) - поле для сортировки (id, resultId)
- `order` (optional) - направление сортировки (asc, desc)

### GET `/api/v1/results/{id}`
Получить результат по ID

### GET `/api/v1/results/function/{resultId}`
Получить все результаты функции

**Параметры пути:**
- `resultId` (required) - ID функции

**Параметры запроса:**
- `sortBy` (optional) - поле для сортировки (id)
- `order` (optional) - направление сортировки (asc, desc)

### GET `/api/v1/results/function/{resultId}/latest`
Получить последний результат функции

**Ответ:**
```json
{
  "success": true,
  "data": {
    "id": 100,
    "resultId": 1,
    "result": "Latest computation result"
  }
}
```

### GET `/api/v1/results/function/{resultId}/count`
Подсчитать количество результатов функции

**Ответ:**
```json
{
  "success": true,
  "data": {
    "result_count": 15
  }
}
```

### GET `/api/v1/results/search?resultLike={pattern}`
Поиск результатов по частичному совпадению текста

**Параметры запроса:**
- `resultLike` (required) - паттерн для поиска

### GET `/api/v1/results/function/{resultId}/search?resultLike={pattern}`
Поиск результатов функции по частичному совпадению текста

**Параметры запроса:**
- `resultLike` (required) - паттерн для поиска

### POST `/api/v1/results`
Создать результат

**Тело запроса:**
```json
{
  "resultId": 1,
  "result": "Computation result: 42.5"
}
```

**Ответ:**
```json
{
  "success": true,
  "data": {
    "id": 20,
    "resultId": 1,
    "result": "Computation result: 42.5"
  },
  "message": "Результат успешно создан"
}
```

### POST `/api/v1/results/batch`
Создать несколько результатов

**Тело запроса:**
```json
{
  "resultId": 1,
  "results": [
    "Result 1",
    "Result 2",
    "Result 3"
  ]
}
```

**Ответ:**
```json
{
  "success": true,
  "data": {
    "created": 3,
    "results": [
      {"id": 21, "resultId": 1, "result": "Result 1"},
      {"id": 22, "resultId": 1, "result": "Result 2"},
      {"id": 23, "resultId": 1, "result": "Result 3"}
    ]
  },
  "message": "Создано результатов: 3"
}
```

### PUT `/api/v1/results/{id}`
Обновить результат

**Тело запроса:**
```json
{
  "result": "Updated result"
}
```

### PATCH `/api/v1/results/function/{resultId}`
Обновить все результаты функции

**Тело запроса:**
```json
{
  "result": "New result text"
}
```

### PATCH `/api/v1/results/function/{resultId}/latest`
Обновить последний результат функции

**Тело запроса:**
```json
{
  "result": "Updated latest result"
}
```

### DELETE `/api/v1/results/{id}`
Удалить результат по ID

### DELETE `/api/v1/results/function/{resultId}`
Удалить все результаты функции

### DELETE `/api/v1/results/function/{resultId}/latest`
Удалить последний результат функции

### DELETE `/api/v1/results/function/{resultId}/search?resultLike={pattern}`
Удалить результаты функции по частичному совпадению текста

**Параметры запроса:**
- `resultLike` (required) - паттерн для поиска

---

## 5. API для системы поиска

### POST `/api/v1/search/dfs`
Поиск в глубину (Depth-First Search)

**Тело запроса:**
```json
{
  "criteria": {
    "fieldName": "login",
    "value": "user",
    "operator": "CONTAINS",
    "sortField": "id",
    "sortDirection": "ASC"
  }
}
```

**Ответ:**
```json
{
  "success": true,
  "data": [
    {
      "type": "UserDTO",
      "value": {
        "id": 1,
        "login": "user1",
        "email": "user1@example.com"
      }
    },
    {
      "type": "FunctionDTO",
      "value": {
        "id": 5,
        "userId": 1,
        "name": "Test Function",
        "type": "linear"
      }
    }
  ],
  "executionTime": 15,
  "total": 2
}
```

### POST `/api/v1/search/bfs`
Поиск в ширину (Breadth-First Search)

**Тело запроса:** (аналогично DFS)

**Ответ:** (аналогично DFS)

### POST `/api/v1/search/hierarchical`
Поиск по иерархии

**Тело запроса:**
```json
{
  "criteria": {
    "fieldName": "login",
    "value": "user",
    "operator": "STARTS_WITH"
  }
}
```

**Ответ:**
```json
{
  "success": true,
  "data": [
    {
      "user": {
        "id": 1,
        "login": "user1",
        "email": "user1@example.com"
      },
      "functions": [
        {
          "function": {
            "id": 5,
            "userId": 1,
            "name": "Test Function",
            "type": "linear"
          },
          "points": [
            {
              "id": 10,
              "funcId": 5,
              "xValue": 1.0,
              "yValue": 10.0
            }
          ],
          "results": [
            {
              "id": 20,
              "resultId": 5,
              "result": "Computation result"
            }
          ]
        }
      ]
    }
  ],
  "executionTime": 20,
  "total": 1
}
```

### POST `/api/v1/search/single`
Одиночный поиск

**Тело запроса:**
```json
{
  "entityType": "UserDTO",
  "criteria": {
    "fieldName": "login",
    "value": "test",
    "operator": "STARTS_WITH",
    "sortField": "id",
    "sortDirection": "ASC"
  }
}
```

**Ответ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "login": "testuser",
      "email": "test@example.com"
    }
  ],
  "executionTime": 5,
  "total": 1
}
```

### POST `/api/v1/search/multiple`
Множественный поиск

**Тело запроса:**
```json
{
  "entityType": "FunctionDTO",
  "criteria": {
    "multipleCriteria": [
      {
        "fieldName": "type",
        "value": "linear",
        "operator": "EQUALS"
      },
      {
        "fieldName": "name",
        "value": "func",
        "operator": "CONTAINS"
      }
    ],
    "sortField": "name",
    "sortDirection": "ASC"
  }
}
```

**Ответ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "userId": 1,
      "name": "Linear Function",
      "type": "linear"
    }
  ],
  "executionTime": 8,
  "total": 1
}
```

### Операторы поиска

Доступные операторы:
- `EQUALS` - точное совпадение
- `CONTAINS` - содержит подстроку
- `STARTS_WITH` - начинается с
- `ENDS_WITH` - заканчивается на
- `GREATER_THAN` - больше (для чисел)
- `LESS_THAN` - меньше (для чисел)
- `GREATER_OR_EQUAL` - больше или равно (для чисел)
- `LESS_OR_EQUAL` - меньше или равно (для чисел)

### Направления сортировки

- `ASC` - по возрастанию
- `DESC` - по убыванию

### Поддерживаемые типы сущностей для поиска

- `UserDTO`
- `FunctionDTO`
- `PointDTO`
- `ResultDTO`

---

## Коды ошибок

### Общие ошибки
- `VALIDATION_ERROR` - ошибка валидации данных
- `NOT_FOUND` - ресурс не найден
- `UNAUTHORIZED` - требуется аутентификация
- `FORBIDDEN` - недостаточно прав доступа
- `INTERNAL_ERROR` - внутренняя ошибка сервера

### Специфичные ошибки
- `USER_NOT_FOUND` - пользователь не найден
- `FUNCTION_NOT_FOUND` - функция не найдена
- `POINT_NOT_FOUND` - точка не найдена
- `RESULT_NOT_FOUND` - результат не найден
- `DUPLICATE_LOGIN` - пользователь с таким login уже существует
- `INVALID_SEARCH_CRITERIA` - неверные критерии поиска

---

## Примеры использования

### Пример 1: Создание пользователя и функции
```bash
# 1. Создать пользователя
POST /api/v1/users
{
  "login": "developer",
  "password": "secure123",
  "email": "dev@example.com"
}

# 2. Создать функцию для пользователя
POST /api/v1/functions
{
  "userId": 1,
  "name": "Quadratic Function",
  "type": "quadratic"
}

# 3. Добавить точки к функции
POST /api/v1/points/batch
{
  "funcId": 1,
  "points": [
    {"xValue": 0.0, "yValue": 0.0},
    {"xValue": 1.0, "yValue": 1.0},
    {"xValue": 2.0, "yValue": 4.0}
  ]
}
```

### Пример 2: Поиск и сортировка
```bash
# Поиск функций пользователя с сортировкой
GET /api/v1/functions/user/1?sortBy=name&order=asc

# Поиск в глубину с критериями
POST /api/v1/search/dfs
{
  "criteria": {
    "fieldName": "type",
    "value": "linear",
    "operator": "EQUALS",
    "sortField": "id",
    "sortDirection": "ASC"
  }
}
```

### Пример 3: Обновление данных
```bash
# Обновить email пользователя
PATCH /api/v1/users/1/email
{
  "email": "newemail@example.com"
}

# Обновить все точки функции (умножение на коэффициент)
PATCH /api/v1/points/function/1/multiply?coefficient=2.0
```

---

## Примечания

1. Все даты и время должны быть в формате ISO 8601
2. Все числовые значения должны быть валидными (не NaN, не Infinity)
3. Пароли никогда не возвращаются в ответах API
4. При удалении пользователя каскадно удаляются все связанные функции, точки и результаты
5. Все операции поиска поддерживают пагинацию через параметры `limit` и `offset`
6. Время выполнения операций поиска указывается в миллисекундах

---

## Версионирование

Текущая версия API: **v1**

При изменении контракта будет создана новая версия (v2, v3 и т.д.) для обеспечения обратной совместимости.

---

## Контакты и поддержка

Этот контракт используется в лабораторных работах №6 и №7.
Для вопросов и предложений обращайтесь к разработчикам проекта.

