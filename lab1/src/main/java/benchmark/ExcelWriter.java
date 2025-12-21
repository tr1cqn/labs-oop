package benchmark;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Класс для записи результатов бенчмарка в Excel файл
 */
public class ExcelWriter {
    
    /**
     * Сохраняет результаты бенчмарка в Excel файл
     */
    public void saveToExcel(List<BenchmarkResult> results, String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Benchmark Results");
            
            // Стиль для заголовка
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Создаем заголовок
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "Query Name", headerStyle);
            createCell(headerRow, 1, "Duration (ms)", headerStyle);
            createCell(headerRow, 2, "Records Count", headerStyle);
            
            // Заполняем данные
            int rowNum = 1;
            for (BenchmarkResult result : results) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(result.getQueryName());
                row.createCell(1).setCellValue(result.getDuration());
                row.createCell(2).setCellValue(result.getRecordsCount());
            }
            
            // Авто-размер колонок
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Сохраняем файл
            try (FileOutputStream fos = new FileOutputStream(filename)) {
                workbook.write(fos);
                System.out.println("Результаты сохранены в файл: " + filename);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении Excel файла: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Создает таблицу сравнения Manual vs Framework
     */
    public void saveComparisonToExcel(List<BenchmarkResult> manualResults, 
                                      List<BenchmarkResult> frameworkResults, 
                                      String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Performance Comparison");
            
            // Стили
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            
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
            for (int i = 0; i < manualResults.size() && i < frameworkResults.size(); i++) {
                BenchmarkResult manual = manualResults.get(i);
                BenchmarkResult framework = frameworkResults.get(i);
                
                if (manual.getQueryName().equals(framework.getQueryName())) {
                    Row row = sheet.createRow(rowNum++);
                    
                    Cell opCell = row.createCell(0);
                    opCell.setCellValue(manual.getQueryName());
                    
                    Cell manualCell = row.createCell(1);
                    manualCell.setCellValue(manual.getDuration());
                    manualCell.setCellStyle(dataStyle);
                    
                    Cell frameworkCell = row.createCell(2);
                    frameworkCell.setCellValue(framework.getDuration());
                    frameworkCell.setCellStyle(dataStyle);
                    
                    Cell diffCell = row.createCell(3);
                    long diff = manual.getDuration() - framework.getDuration();
                    diffCell.setCellValue(diff);
                    diffCell.setCellStyle(dataStyle);
                }
            }
            
            // Автоматическая ширина колонок
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Сохранение файла
            try (FileOutputStream fos = new FileOutputStream(filename)) {
                workbook.write(fos);
                System.out.println("Таблица сравнения сохранена в файл: " + filename);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении Excel файла: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}

