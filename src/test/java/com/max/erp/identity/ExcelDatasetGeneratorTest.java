package com.max.erp.identity;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelDatasetGeneratorTest {

    @Test
    void generateExcelDatasets() throws Exception {
        Path baseDir = Paths.get("C:/MAX-ERP/data/sample-datasets");
        if (!Files.exists(baseDir)) return;

        String[] files = {"customers", "vendors", "products", "sales_orders"};

        for (String name : files) {
            Path csvPath = baseDir.resolve(name + ".csv");
            Path xlsxPath = baseDir.resolve(name + ".xlsx");

            if (!Files.exists(csvPath)) continue;

            List<String> lines = Files.readAllLines(csvPath);
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(name);

                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                for (int r = 0; r < lines.size(); r++) {
                    String line = lines.get(r);
                    Row row = sheet.createRow(r);

                    String[] tokens = parseCsvLine(line);
                    for (int c = 0; c < tokens.length; c++) {
                        Cell cell = row.createCell(c);
                        String val = tokens[c];

                        if (r == 0) {
                            cell.setCellValue(val);
                            cell.setCellStyle(headerStyle);
                        } else {
                            if (val.matches("^-?\\d+$")) {
                                cell.setCellValue(Long.parseLong(val));
                            } else if (val.matches("^-?\\d+(\\.\\d+)?$")) {
                                cell.setCellValue(Double.parseDouble(val));
                            } else {
                                cell.setCellValue(val);
                            }
                        }
                    }
                }

                for (int c = 0; c < 12; c++) {
                    sheet.autoSizeColumn(c);
                }

                try (OutputStream os = Files.newOutputStream(xlsxPath)) {
                    workbook.write(os);
                }
            }

            assertTrue(Files.exists(xlsxPath));
        }
    }

    private String[] parseCsvLine(String line) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }
}
