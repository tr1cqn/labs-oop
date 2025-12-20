package benchmark;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Класс для сравнения производительности и создания таблицы результатов в Excel
 */
public class PerformanceComparison {
    
    private static final String EXCEL_FILE = "performance_comparison.xlsx";
    
    /**
     * Создает Excel таблицу результатов сравнения
     */
    public static void createComparisonTable(FrameworkPerformanceBenchmark.PerformanceResults frameworkResults,
                                            FrameworkPerformanceBenchmark.PerformanceResults manualResults) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Сравнение производительности");
            
            // Стили
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Заголовок
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Сравнение производительности Manual (JDBC) vs Framework (Hibernate)");
            titleCell.setCellStyle(headerStyle);
            
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Тестирование выполнено на таблицах с минимумом 10,000 записей");
            
            // Пустая строка
            sheet.createRow(2);
            
            // Заголовки таблицы
            Row headerRow = sheet.createRow(3);
            String[] headers = {"Операция", "Manual (JDBC), мс", "Framework (Hibernate), мс", "Разница, мс"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Данные
            int rowNum = 4;
            String[] operations = {
                "Поиск по ID (100 операций)",
                "Поиск всех записей",
                "Поиск с условиями (50 операций)",
                "Вставка (100 записей)",
                "Обновление (100 записей)",
                "Удаление (100 записей)"
            };
            
            long[] manualValues = {
                manualResults.getFindById(),
                manualResults.getFindAll(),
                manualResults.getFindWithConditions(),
                manualResults.getInsert(),
                manualResults.getUpdate(),
                manualResults.getDelete()
            };
            
            long[] frameworkValues = {
                frameworkResults.getFindById(),
                frameworkResults.getFindAll(),
                frameworkResults.getFindWithConditions(),
                frameworkResults.getInsert(),
                frameworkResults.getUpdate(),
                frameworkResults.getDelete()
            };
            
            for (int i = 0; i < operations.length; i++) {
                Row row = sheet.createRow(rowNum++);
                
                Cell opCell = row.createCell(0);
                opCell.setCellValue(operations[i]);
                
                Cell manualCell = row.createCell(1);
                manualCell.setCellValue(manualValues[i]);
                manualCell.setCellStyle(dataStyle);
                
                Cell frameworkCell = row.createCell(2);
                frameworkCell.setCellValue(frameworkValues[i]);
                frameworkCell.setCellStyle(dataStyle);
                
                Cell diffCell = row.createCell(3);
                long diff = manualValues[i] - frameworkValues[i];
                diffCell.setCellValue(diff);
                diffCell.setCellStyle(dataStyle);
            }
            
            // Автоматическая ширина колонок
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Сохранение файла
            try (FileOutputStream fileOut = new FileOutputStream(EXCEL_FILE)) {
                workbook.write(fileOut);
                System.out.println("Excel таблица результатов сохранена в " + EXCEL_FILE);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении Excel файла: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Создает Excel таблицу только с результатами Framework (для случая, когда Manual еще не готов)
     */
    public static void createFrameworkTable(FrameworkPerformanceBenchmark.PerformanceResults frameworkResults) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Framework Results");
            
            // Стили
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Заголовок
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Результаты производительности Framework (Hibernate)");
            titleCell.setCellStyle(headerStyle);
            
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Тестирование выполнено на таблицах с минимумом 10,000 записей");
            
            sheet.createRow(2);
            
            // Заголовки
            Row headerRow = sheet.createRow(3);
            Cell opHeader = headerRow.createCell(0);
            opHeader.setCellValue("Операция");
            opHeader.setCellStyle(headerStyle);
            
            Cell timeHeader = headerRow.createCell(1);
            timeHeader.setCellValue("Время, мс");
            timeHeader.setCellStyle(headerStyle);
            
            // Данные
            int rowNum = 4;
            String[] operations = {
                "Поиск по ID (100 операций)",
                "Поиск всех записей",
                "Поиск с условиями (50 операций)",
                "Вставка (100 записей)",
                "Обновление (100 записей)",
                "Удаление (100 записей)"
            };
            
            long[] values = {
                frameworkResults.getFindById(),
                frameworkResults.getFindAll(),
                frameworkResults.getFindWithConditions(),
                frameworkResults.getInsert(),
                frameworkResults.getUpdate(),
                frameworkResults.getDelete()
            };
            
            for (int i = 0; i < operations.length; i++) {
                Row row = sheet.createRow(rowNum++);
                Cell opCell = row.createCell(0);
                opCell.setCellValue(operations[i]);
                
                Cell valueCell = row.createCell(1);
                valueCell.setCellValue(values[i]);
                valueCell.setCellStyle(dataStyle);
            }
            
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            
            try (FileOutputStream fileOut = new FileOutputStream("performance_framework.xlsx")) {
                workbook.write(fileOut);
                System.out.println("Excel таблица Framework сохранена в performance_framework.xlsx");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении Excel файла: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Также обновляем общий файл сравнения с результатами Framework
        updateComparisonTableWithFramework(frameworkResults);
    }
    
    /**
     * Обновляет Excel файл performance_comparison.xlsx с результатами Framework
     */
    public static void updateComparisonTableWithFramework(FrameworkPerformanceBenchmark.PerformanceResults frameworkResults) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Сравнение производительности");
            
            // Стили
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Заголовок
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Сравнение производительности Manual (JDBC) vs Framework (Hibernate)");
            titleCell.setCellStyle(headerStyle);
            
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Тестирование выполнено на таблицах с минимумом 10,000 записей");
            
            sheet.createRow(2);
            
            // Заголовки таблицы
            Row headerRow = sheet.createRow(3);
            String[] headers = {"Операция", "Manual (JDBC), мс", "Framework (Hibernate), мс", "Разница, мс"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Данные
            int rowNum = 4;
            String[] operations = {
                "Поиск по ID (100 операций)",
                "Поиск всех записей",
                "Поиск с условиями (50 операций)",
                "Вставка (100 записей)",
                "Обновление (100 записей)",
                "Удаление (100 записей)"
            };
            
            long[] frameworkValues = {
                frameworkResults.getFindById(),
                frameworkResults.getFindAll(),
                frameworkResults.getFindWithConditions(),
                frameworkResults.getInsert(),
                frameworkResults.getUpdate(),
                frameworkResults.getDelete()
            };
            
            for (int i = 0; i < operations.length; i++) {
                Row row = sheet.createRow(rowNum++);
                
                Cell opCell = row.createCell(0);
                opCell.setCellValue(operations[i]);
                
                Cell manualCell = row.createCell(1);
                manualCell.setCellValue("-");
                manualCell.setCellStyle(dataStyle);
                
                Cell frameworkCell = row.createCell(2);
                frameworkCell.setCellValue(frameworkValues[i]);
                frameworkCell.setCellStyle(dataStyle);
                
                Cell diffCell = row.createCell(3);
                diffCell.setCellValue("-");
                diffCell.setCellStyle(dataStyle);
            }
            
            // Автоматическая ширина колонок
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Сохранение файла
            try (FileOutputStream fileOut = new FileOutputStream(EXCEL_FILE)) {
                workbook.write(fileOut);
                System.out.println("Excel файл " + EXCEL_FILE + " обновлен с результатами Framework");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении Excel файла: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Обновляет Excel файл performance_comparison.xlsx с результатами Manual
     */
    public static void updateComparisonTableWithManual(FrameworkPerformanceBenchmark.PerformanceResults manualResults) {
        try {
            // Пытаемся прочитать существующий файл
            Workbook workbook;
            Sheet sheet;
            boolean fileExists = new java.io.File(EXCEL_FILE).exists();
            
            if (fileExists) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(EXCEL_FILE)) {
                    workbook = new XSSFWorkbook(fis);
                    sheet = workbook.getSheetAt(0);
                }
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Сравнение производительности");
            }
            
            // Стили
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Если файл новый, создаем заголовки
            if (!fileExists) {
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("Сравнение производительности Manual (JDBC) vs Framework (Hibernate)");
                titleCell.setCellStyle(headerStyle);
                
                Row subtitleRow = sheet.createRow(1);
                Cell subtitleCell = subtitleRow.createCell(0);
                subtitleCell.setCellValue("Тестирование выполнено на таблицах с минимумом 10,000 записей");
                
                sheet.createRow(2);
                
                Row headerRow = sheet.createRow(3);
                String[] headers = {"Операция", "Manual (JDBC), мс", "Framework (Hibernate), мс", "Разница, мс"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
            }
            
            // Обновляем данные Manual и вычисляем разницу
            String[] operations = {
                "Поиск по ID (100 операций)",
                "Поиск всех записей",
                "Поиск с условиями (50 операций)",
                "Вставка (100 записей)",
                "Обновление (100 записей)",
                "Удаление (100 записей)"
            };
            
            long[] manualValues = {
                manualResults.getFindById(),
                manualResults.getFindAll(),
                manualResults.getFindWithConditions(),
                manualResults.getInsert(),
                manualResults.getUpdate(),
                manualResults.getDelete()
            };
            
            int startRow = fileExists ? 4 : 4;
            for (int i = 0; i < operations.length; i++) {
                Row row = sheet.getRow(startRow + i);
                if (row == null) {
                    row = sheet.createRow(startRow + i);
                }
                
                // Обновляем Manual значения
                Cell manualCell = row.getCell(1);
                if (manualCell == null) {
                    manualCell = row.createCell(1);
                }
                manualCell.setCellValue(manualValues[i]);
                manualCell.setCellStyle(dataStyle);
                
                // Читаем Framework значение и вычисляем разницу
                Cell frameworkCell = row.getCell(2);
                if (frameworkCell != null && frameworkCell.getCellType() == CellType.NUMERIC) {
                    long frameworkValue = (long) frameworkCell.getNumericCellValue();
                    long diff = manualValues[i] - frameworkValue;
                    
                    Cell diffCell = row.getCell(3);
                    if (diffCell == null) {
                        diffCell = row.createCell(3);
                    }
                    diffCell.setCellValue(diff);
                    diffCell.setCellStyle(dataStyle);
                }
            }
            
            // Автоматическая ширина колонок
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Сохранение файла
            try (FileOutputStream fileOut = new FileOutputStream(EXCEL_FILE)) {
                workbook.write(fileOut);
                System.out.println("Excel файл " + EXCEL_FILE + " обновлен с результатами Manual");
            }
            
            workbook.close();
        } catch (IOException e) {
            System.err.println("Ошибка при обновлении Excel файла: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
