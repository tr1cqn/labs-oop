# Скрипт для запуска Newman тестов и измерения производительности API


param(
    [string]$Branch = "both",  
    [int]$Iterations = 5       
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$ResultsDir = Join-Path $ProjectRoot "newman-results"
$ManualCollection = Join-Path $ProjectRoot "postman\manual\api-tests.postman_collection.json"
$FrameworkCollection = Join-Path $ProjectRoot "postman\framework\api-tests.postman_collection.json"

# Создаем директорию для результатов
New-Item -ItemType Directory -Force -Path $ResultsDir | Out-Null

$Results = @{
    manual = @{}
    framework = @{}
}

function Run-NewmanTests {
    param(
        [string]$CollectionPath,
        [string]$BranchName,
        [int]$Iterations
    )
    
    Write-Host "`n=== Запуск тестов для ветки: $BranchName ===" -ForegroundColor Cyan
    
    $BranchResults = @{}
    
    for ($i = 1; $i -le $Iterations; $i++) {
        Write-Host "Итерация $i из $Iterations..." -ForegroundColor Yellow
        
        $OutputFile = Join-Path $ResultsDir "$BranchName-iteration-$i.json"
        
        # Запускаем Newman с JSON репортером
        $newmanOutput = newman run $CollectionPath `
            --reporter json `
            --reporter-json-export $OutputFile `
            2>&1
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Ошибка при запуске Newman: $newmanOutput" -ForegroundColor Red
            continue
        }
        
        # Парсим результаты
        if (Test-Path $OutputFile) {
            $jsonContent = Get-Content $OutputFile -Raw | ConvertFrom-Json
            
            foreach ($run in $jsonContent.run.executions) {
                $requestName = $run.item.name
                $responseTime = $run.response.responseTime
                $statusCode = $run.response.code
                
                if (-not $BranchResults.ContainsKey($requestName)) {
                    $BranchResults[$requestName] = @{
                        times = @()
                        statusCodes = @()
                        success = 0
                        total = 0
                    }
                }
                
                $BranchResults[$requestName].times += $responseTime
                $BranchResults[$requestName].statusCodes += $statusCode
                $BranchResults[$requestName].total++
                
                if ($statusCode -ge 200 -and $statusCode -lt 300) {
                    $BranchResults[$requestName].success++
                }
            }
        }
    }
    
    # Вычисляем средние значения
    $Summary = @{}
    foreach ($requestName in $BranchResults.Keys) {
        $times = $BranchResults[$requestName].times
        if ($times.Count -gt 0) {
            $avgTime = ($times | Measure-Object -Average).Average
            $minTime = ($times | Measure-Object -Minimum).Minimum
            $maxTime = ($times | Measure-Object -Maximum).Maximum
            $successRate = ($BranchResults[$requestName].success / $BranchResults[$requestName].total) * 100
            
            $Summary[$requestName] = @{
                avgTime = [math]::Round($avgTime, 2)
                minTime = [math]::Round($minTime, 2)
                maxTime = [math]::Round($maxTime, 2)
                successRate = [math]::Round($successRate, 2)
                iterations = $BranchResults[$requestName].total
            }
        }
    }
    
    return $Summary
}

# Запускаем тесты для manual ветки
if ($Branch -eq "manual" -or $Branch -eq "both") {
    if (Test-Path $ManualCollection) {
        $Results.manual = Run-NewmanTests -CollectionPath $ManualCollection -BranchName "manual" -Iterations $Iterations
    } else {
        Write-Host "Коллекция для manual ветки не найдена: $ManualCollection" -ForegroundColor Red
    }
}

# Запускаем тесты для framework ветки
if ($Branch -eq "framework" -or $Branch -eq "both") {
    if (Test-Path $FrameworkCollection) {
        $Results.framework = Run-NewmanTests -CollectionPath $FrameworkCollection -BranchName "framework" -Iterations $Iterations
    } else {
        Write-Host "Коллекция для framework ветки не найдена: $FrameworkCollection" -ForegroundColor Red
    }
}

# Сохраняем результаты в JSON
$ResultsJson = Join-Path $ResultsDir "performance-results.json"
$Results | ConvertTo-Json -Depth 10 | Out-File $ResultsJson -Encoding UTF8
Write-Host "`nРезультаты сохранены в: $ResultsJson" -ForegroundColor Green

# Генерируем таблицу сравнения
$TableContent = Generate-ComparisonTable -Results $Results
$TableFile = Join-Path $ProjectRoot "api_performance_comparison.md"
$TableContent | Out-File $TableFile -Encoding UTF8
Write-Host "Таблица сравнения сохранена в: $TableFile" -ForegroundColor Green

Write-Host "`n=== Тестирование завершено ===" -ForegroundColor Cyan

function Generate-ComparisonTable {
    param([hashtable]$Results)
    
    $table = @"
# Сравнение производительности API запросов

## Результаты тестирования


Количество итераций: $Iterations



## Сравнительная таблица

| Endpoint | Method | Manual Avg (ms) | Manual Min (ms) | Manual Max (ms) | Manual Success (%) | Framework Avg (ms) | Framework Min (ms) | Framework Max (ms) | Framework Success (%) | Разница (ms) | Улучшение (%) |
|----------|--------|-----------------|----------------|-----------------|-------------------|-------------------|-------------------|-------------------|---------------------|--------------|---------------|
"@

    # Получаем все уникальные эндпоинты
    $allEndpoints = @()
    if ($Results.manual) {
        $allEndpoints += $Results.manual.Keys
    }
    if ($Results.framework) {
        $allEndpoints += $Results.framework.Keys
    }
    $allEndpoints = $allEndpoints | Select-Object -Unique | Sort-Object

    foreach ($endpoint in $allEndpoints) {
        $manualData = $Results.manual[$endpoint]
        $frameworkData = $Results.framework[$endpoint]
        
        $manualAvg = if ($manualData) { $manualData.avgTime } else { "-" }
        $manualMin = if ($manualData) { $manualData.minTime } else { "-" }
        $manualMax = if ($manualData) { $manualData.maxTime } else { "-" }
        $manualSuccess = if ($manualData) { "$($manualData.successRate)%" } else { "-" }
        
        $frameworkAvg = if ($frameworkData) { $frameworkData.avgTime } else { "-" }
        $frameworkMin = if ($frameworkData) { $frameworkData.minTime } else { "-" }
        $frameworkMax = if ($frameworkData) { $frameworkData.maxTime } else { "-" }
        $frameworkSuccess = if ($frameworkData) { "$($frameworkData.successRate)%" } else { "-" }
        
        # Вычисляем разницу
        $diff = "-"
        $improvement = "-"
        if ($manualData -and $frameworkData) {
            $diff = [math]::Round($frameworkData.avgTime - $manualData.avgTime, 2)
            if ($manualData.avgTime -gt 0) {
                $improvement = [math]::Round((($manualData.avgTime - $frameworkData.avgTime) / $manualData.avgTime) * 100, 2)
            }
        }
        
        # Определяем метод из названия
        $method = "GET"
        if ($endpoint -match "Create|POST") { $method = "POST" }
        elseif ($endpoint -match "Update|PUT") { $method = "PUT" }
        elseif ($endpoint -match "Delete|DELETE") { $method = "DELETE" }
        elseif ($endpoint -match "PATCH") { $method = "PATCH" }
        
        $table += "`n| $endpoint | $method | $manualAvg | $manualMin | $manualMax | $manualSuccess | $frameworkAvg | $frameworkMin | $frameworkMax | $frameworkSuccess | $diff | $improvement |"
    }

    $table += @"



### Общая статистика

"@

    if ($Results.manual -and $Results.framework) {
        $manualAvgAll = ($Results.manual.Values | ForEach-Object { $_.avgTime } | Measure-Object -Average).Average
        $frameworkAvgAll = ($Results.framework.Values | ForEach-Object { $_.avgTime } | Measure-Object -Average).Average
        
        $table += @"

- **Среднее время выполнения (Manual)**: $([math]::Round($manualAvgAll, 2)) мс
- **Среднее время выполнения (Framework)**: $([math]::Round($frameworkAvgAll, 2)) мс
- **Общее улучшение**: $([math]::Round((($manualAvgAll - $frameworkAvgAll) / $manualAvgAll) * 100, 2))%

"@
    }

    $table += @"

"@

    return $table
}

