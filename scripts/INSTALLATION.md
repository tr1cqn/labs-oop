# Инструкция по установке Newman

## Требования

Для работы скрипта тестирования API необходимо установить:

1. **Node.js** (включает npm)
2. **Newman** (CLI инструмент для Postman)

## Шаг 1: Установка Node.js

### Windows

1. Перейдите на официальный сайт: https://nodejs.org/
2. Скачайте LTS версию (рекомендуется)
3. Запустите установщик и следуйте инструкциям
4. **Важно**: Убедитесь, что опция "Add to PATH" отмечена при установке
5. Перезапустите PowerShell или командную строку

### Проверка установки

Откройте PowerShell и выполните:

```powershell
node --version
npm --version
```

Должны отобразиться версии Node.js и npm.

## Шаг 2: Установка Newman

После установки Node.js выполните:

```powershell
npm install -g newman newman-reporter-html
```

Флаг `-g` означает глобальную установку (доступна из любой директории).

### Проверка установки

```powershell
newman --version
```

Должна отобразиться версия Newman.

## Шаг 3: Запуск тестов

После установки можно запускать скрипт:

```powershell
.\scripts\run_newman_tests.ps1
```

## Решение проблем

### Ошибка "npm не распознано"

**Причина**: Node.js не установлен или не добавлен в PATH.

**Решение**:
1. Переустановите Node.js с официального сайта
2. При установке убедитесь, что опция "Add to PATH" отмечена
3. Перезапустите PowerShell/командную строку
4. Проверьте переменную PATH: `$env:PATH`

### Ошибка "newman не распознано"

**Причина**: Newman не установлен глобально.

**Решение**:
1. Убедитесь, что npm работает: `npm --version`
2. Установите Newman: `npm install -g newman newman-reporter-html`
3. Перезапустите PowerShell
4. Проверьте: `newman --version`

### Проблемы с правами доступа

Если возникают ошибки доступа при установке:

**Windows**:
- Запустите PowerShell от имени администратора
- Или используйте: `npm install -g newman newman-reporter-html --force`

## Альтернативные способы тестирования

Если установка Node.js невозможна, можно использовать:

1. **Postman Desktop App** - импортируйте коллекции и запускайте тесты вручную
2. **Postman Web** - используйте браузерную версию Postman
3. **cURL** - для простых запросов можно использовать curl команды

## Дополнительная информация

- Официальный сайт Node.js: https://nodejs.org/
- Документация Newman: https://learning.postman.com/docs/running-collections/using-newman-cli/command-line-integration-with-newman/
- Postman коллекции находятся в: `postman/manual/` и `postman/framework/`

