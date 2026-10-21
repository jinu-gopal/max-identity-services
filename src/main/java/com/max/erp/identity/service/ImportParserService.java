package com.max.erp.identity.service;

import com.max.erp.identity.entity.ConfigurationImportJob;
import com.max.erp.identity.entity.ConfigurationImportRow;
import com.max.erp.identity.enums.ImportJobStatus;
import com.max.erp.identity.enums.ImportRowStatus;
import com.max.erp.identity.repository.ConfigurationImportJobRepository;
import com.max.erp.identity.repository.ConfigurationImportRowRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ImportParserService {

    private final ConfigurationImportJobRepository importJobRepository;
    private final ConfigurationImportRowRepository importRowRepository;
    private final Path storageRoot;

    public ImportParserService(
            ConfigurationImportJobRepository importJobRepository,
            ConfigurationImportRowRepository importRowRepository,
            @Value("${max.erp.import.storage-directory:./data/imports}")
            String storageDirectory) {

        this.importJobRepository = importJobRepository;
        this.importRowRepository = importRowRepository;
        this.storageRoot = Paths.get(storageDirectory)
                .toAbsolutePath()
                .normalize();
    }

    @Transactional
    public int parse(UUID importJobId) {

        ConfigurationImportJob job =
                importJobRepository.findById(importJobId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Import job not found: " + importJobId));

        Path filePath = storageRoot
                .resolve(importJobId.toString())
                .resolve("original")
                .resolve(job.getFileName())
                .normalize();

        Path jobDirectory = storageRoot
                .resolve(importJobId.toString())
                .resolve("original")
                .normalize();

        if (!filePath.startsWith(jobDirectory)) {
            markFailed(job);
            throw new IllegalArgumentException("Invalid import file path");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            markFailed(job);
            throw new IllegalArgumentException("Stored import file not found");
        }

        importRowRepository.deleteAll(
                importRowRepository.findAllByImportJobIdOrderByRowNumberAsc(importJobId)
        );

        job.setStatus(ImportJobStatus.MAPPING);
        importJobRepository.save(job);

        try {
            int rowCount;

            if ("CSV".equalsIgnoreCase(job.getFileType())) {
                rowCount = parseCsv(job, filePath);
            } else if ("XLSX".equalsIgnoreCase(job.getFileType())) {
                rowCount = parseXlsx(job, filePath);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported import file type: " + job.getFileType());
            }

            job.setTotalRows(rowCount);
            job.setStatus(ImportJobStatus.MAPPING);
            importJobRepository.save(job);

            return rowCount;

        } catch (IOException exception) {

            markFailed(job);

            throw new IllegalArgumentException(
                    "Unable to parse import file", exception);
        }
    }

    private int parseCsv(
            ConfigurationImportJob job,
            Path filePath) throws IOException {

        try (
                Reader reader = Files.newBufferedReader(
                        filePath,
                        StandardCharsets.UTF_8);

                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)
        ) {

            List<String> headers = normalizeHeaders(parser.getHeaderNames());

            int rowNumber = 1;
            int storedRows = 0;

            for (CSVRecord record : parser) {

                rowNumber++;

                Map<String, Object> rawData =
                        new LinkedHashMap<>();

                for (int index = 0; index < headers.size(); index++) {
                    String header = headers.get(index);

                    String value =
                            index < record.size()
                                    ? record.get(index)
                                    : "";

                    rawData.put(header, value);
                }

                saveRow(
                        job,
                        rowNumber,
                        rawData);

                storedRows++;
            }

            return storedRows;
        }
    }

    private int parseXlsx(
            ConfigurationImportJob job,
            Path filePath) throws IOException {

        try (InputStream inputStream =
                     Files.newInputStream(filePath);
             Workbook workbook =
                     new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());

            if (headerRow == null) {
                return 0;
            }

            DataFormatter formatter =
                    new DataFormatter();

            List<String> headers =
                    readXlsxHeaders(headerRow, formatter);

            int storedRows = 0;

            for (
                    int rowIndex = headerRow.getRowNum() + 1;
                    rowIndex <= sheet.getLastRowNum();
                    rowIndex++
            ) {

                Row row = sheet.getRow(rowIndex);

                if (row == null || isEmptyRow(row, headers.size(), formatter)) {
                    continue;
                }

                Map<String, Object> rawData =
                        new LinkedHashMap<>();

                for (int columnIndex = 0;
                     columnIndex < headers.size();
                     columnIndex++) {

                    Cell cell =
                            row.getCell(
                                    columnIndex,
                                    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                    String value =
                            cell == null
                                    ? ""
                                    : formatter.formatCellValue(cell);

                    rawData.put(headers.get(columnIndex), value);
                }

                saveRow(
                        job,
                        rowIndex + 1,
                        rawData);

                storedRows++;
            }

            return storedRows;
        }
    }

    private List<String> readXlsxHeaders(
            Row headerRow,
            DataFormatter formatter) {

        List<String> headers =
                new ArrayList<>();

        int lastCellNumber =
                headerRow.getLastCellNum();

        for (int columnIndex = 0;
             columnIndex < lastCellNumber;
             columnIndex++) {

            Cell cell =
                    headerRow.getCell(
                            columnIndex,
                            Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            String header =
                    cell == null
                            ? ""
                            : formatter.formatCellValue(cell);

            headers.add(normalizeHeader(header));
        }

        return headers;
    }

    private boolean isEmptyRow(
            Row row,
            int columnCount,
            DataFormatter formatter) {

        for (int columnIndex = 0;
             columnIndex < columnCount;
             columnIndex++) {

            Cell cell =
                    row.getCell(
                            columnIndex,
                            Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (cell != null
                    && !formatter.formatCellValue(cell)
                    .trim()
                    .isEmpty()) {

                return false;
            }
        }

        return true;
    }

    private List<String> normalizeHeaders(
            List<String> headers) {

        List<String> normalized =
                new ArrayList<>();

        for (String header : headers) {
            normalized.add(normalizeHeader(header));
        }

        return normalized;
    }

    private String normalizeHeader(String header) {

        if (header == null) {
            return "";
        }

        return header
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private void saveRow(
            ConfigurationImportJob job,
            int rowNumber,
            Map<String, Object> rawData) {

        ConfigurationImportRow row =
                new ConfigurationImportRow();

        row.setImportJobId(job.getId());
        row.setRowNumber(rowNumber);
        row.setStatus(ImportRowStatus.PENDING);
        row.setRawData(rawData);

        importRowRepository.save(row);
    }

    private void markFailed(
            ConfigurationImportJob job) {

        job.setStatus(ImportJobStatus.FAILED);
        importJobRepository.save(job);
    }
}
