# Скрипт для проверки работоспособности API

$baseUrl = "http://localhost:8081"
$adminAuth = "Basic YWRtaW46YWRtaW4="  # admin:admin в Base64

Write-Host "=== Тестирование API ===" -ForegroundColor Cyan

# 1. Health Check
Write-Host "`n1. Health Check..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/v1/test" -Method GET -UseBasicParsing
    Write-Host "✓ Health Check: $($response.StatusCode) - $($response.Content)" -ForegroundColor Green
} catch {
    Write-Host "✗ Health Check failed: $_" -ForegroundColor Red
    exit 1
}

# 2. Создание пользователя
Write-Host "`n2. Создание пользователя..." -ForegroundColor Yellow
$userData = @{
    login = "testuser"
    password = "testpass"
    email = "test@example.com"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/v1/users" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $userData `
        -UseBasicParsing
    Write-Host "✓ Пользователь создан: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "  Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Создание пользователя failed: $_" -ForegroundColor Red
}

# 3. Получение списка пользователей
Write-Host "`n3. Получение списка пользователей..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/v1/users" `
        -Method GET `
        -Headers @{"Authorization"=$adminAuth} `
        -UseBasicParsing
    Write-Host "✓ Список пользователей получен: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "  Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Получение списка пользователей failed: $_" -ForegroundColor Red
}

# 4. Получение пользователя по ID
Write-Host "`n4. Получение пользователя по ID (1)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/v1/users/1" `
        -Method GET `
        -Headers @{"Authorization"=$adminAuth} `
        -UseBasicParsing
    Write-Host "✓ Пользователь получен: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "  Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Получение пользователя failed: $_" -ForegroundColor Red
}

# 5. Получение функций
Write-Host "`n5. Получение списка функций..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/v1/functions" `
        -Method GET `
        -Headers @{"Authorization"=$adminAuth} `
        -UseBasicParsing
    Write-Host "✓ Список функций получен: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "  Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Получение функций failed: $_" -ForegroundColor Red
}

Write-Host "`n=== Тестирование завершено ===" -ForegroundColor Cyan

