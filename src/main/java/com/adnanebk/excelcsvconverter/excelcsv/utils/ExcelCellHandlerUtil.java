package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ExcelCellHandlerUtil<T> {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Map<String, BiFunction<Cell, Class<?>, Object>> cellValueMap = new HashMap<>();
    private final Map<String, BiConsumer<Cell, Object>> cellValueSetterMap = new HashMap<>();
    private final DateParserFormatterUtil<T> dateParserFormatterUtil;


    public ExcelCellHandlerUtil(DateParserFormatterUtil<T> dateParserFormatterUtil) {
        this.dateParserFormatterUtil = dateParserFormatterUtil;
        initCellValueMap();
        initValueSetterMap();
    }

    public Object getCellValue(Class<?> type, Cell cell) {
        try {
            var function = cellValueMap.get(getTypeName(type));
            if(function==null)
                throw new ExcelValidationException("Unsupported field type");
            return function.apply(cell, type);
        } catch (IllegalStateException | NumberFormatException e) {
            throw new ExcelValidationException(String.format("Invalid format in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex())));
        }
    }

    public void setCellValue(Class<?> type, Cell cell, Object value) {
        var function = cellValueSetterMap.get(getTypeName(type));
        if(function==null)
            throw new ExcelValidationException("Unsupported field type");
        function.accept(cell, value);
    }

    private String getTypeName(Class<?> type) {
        return type.isEnum() ? Enum.class.getSimpleName().toLowerCase() : type.getSimpleName().toLowerCase();
    }

    private void initCellValueMap() {
        cellValueMap.put(String.class.getSimpleName().toLowerCase(), (cell, fieldType) -> cell.getStringCellValue());
        cellValueMap.put(boolean.class.getSimpleName().toLowerCase(), (cell, fieldType) -> cell.getBooleanCellValue());
        cellValueMap.put(Enum.class.getSimpleName().toLowerCase(), (cell, fieldType) -> Enum.valueOf(fieldType.asSubclass(Enum.class), cell.getStringCellValue()));
        cellValueMap.put(Integer.class.getSimpleName().toLowerCase(), (cell, fieldType) -> (int) cell.getNumericCellValue());
        cellValueMap.put(int.class.getSimpleName().toLowerCase(), (cell, fieldType) -> (int) cell.getNumericCellValue());
        cellValueMap.put(short.class.getSimpleName().toLowerCase(), (cell, fieldType) -> (short) cell.getNumericCellValue());
        cellValueMap.put(long.class.getSimpleName().toLowerCase(), (cell, fieldType) -> (long) cell.getNumericCellValue());
        cellValueMap.put(double.class.getSimpleName().toLowerCase(), (cell, fieldType) -> cell.getNumericCellValue());
        cellValueMap.put(LocalDate.class.getSimpleName().toLowerCase(), (cell, fieldType) -> getAsLocalDate(cell));
        cellValueMap.put(LocalDateTime.class.getSimpleName().toLowerCase(), (cell, fieldType) -> getAsLocalDateTime(cell));
        cellValueMap.put(ZonedDateTime.class.getSimpleName().toLowerCase(), (cell, fieldType) -> getAsZonedDateTime(cell));
        cellValueMap.put(Date.class.getSimpleName().toLowerCase(), (cell, fieldType) -> getAsDate(cell));
    }

    private void initValueSetterMap() {
        cellValueSetterMap.put(String.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(value.toString()));
        cellValueSetterMap.put(Double.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(double.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(Integer.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(int.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(long.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(short.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(boolean.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue((boolean) value));
        cellValueSetterMap.put(Enum.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(value.toString()));
        cellValueSetterMap.put(LocalDate.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(dateParserFormatterUtil.format((LocalDate)value)));
        cellValueSetterMap.put(LocalDateTime.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(dateParserFormatterUtil.format((LocalDateTime) value)));
        cellValueSetterMap.put(ZonedDateTime.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(dateParserFormatterUtil.format((ZonedDateTime) value)));
        cellValueSetterMap.put(Date.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(dateParserFormatterUtil.format((Date) value)));
    }

    private Date getAsDate(Cell cell) {
        try {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getDateCellValue();
            return dateParserFormatterUtil.parseToDate(cell.getStringCellValue());
        } catch (ParseException e) {
            throw new ExcelValidationException(getInvalidCellDateMsg(cell));
        }
    }

    private LocalDate getAsLocalDate(Cell cell) {
        try {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getLocalDateTimeCellValue().toLocalDate();
            return dateParserFormatterUtil.parseToLocalDate(cell.getStringCellValue());
        } catch (DateTimeParseException ex) {
            throw new ExcelValidationException(getInvalidCellDateMsg(cell));
        }
    }

    private LocalDateTime getAsLocalDateTime(Cell cell) {
        try {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getLocalDateTimeCellValue();
            return dateParserFormatterUtil.parseToLocalDateTime(cell.getStringCellValue());
        } catch (DateTimeParseException ex) {
            throw new ExcelValidationException(getInvalidCellDateMsg(cell));
        }
    }

    private ZonedDateTime getAsZonedDateTime(Cell cell) {
        try {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault());
            return dateParserFormatterUtil.parseToZonedDateTime(cell.getStringCellValue());
        } catch (DateTimeParseException ex) {
            throw new ExcelValidationException(getInvalidCellDateMsg(cell));
        }
    }

    private Date convertToDate(LocalDateTime dateToConvert) {
        return java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault())
                .toInstant());
    }

    private Date convertToDate(LocalDate dateToConvert) {
        return Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    private Date convertToDate(ZonedDateTime dateToConvert) {
        return java.util.Date.from(dateToConvert.toInstant());
    }

    private static String getInvalidCellDateMsg(Cell cell) {
        return String.format("Invalid or unsupported date format in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex()));
    }

}
