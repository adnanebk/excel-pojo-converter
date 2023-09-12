package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import com.adnanebk.excelcsvconverter.excelcsv.models.Field;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ExcelRowsHandlerUtil<T> {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Map<String, Function<Cell, Object>> cellValueMap = new HashMap<>();
    private final Map<String, BiConsumer<Cell, Object>> cellValueSetterMap = new HashMap<>();
    private final DateParserFormatterUtil dateParserFormatterUtil;
    private final ReflectionUtil<T> reflectionUtil;


    public ExcelRowsHandlerUtil(ReflectionUtil<T> reflectionUtil) {
        this.reflectionUtil = reflectionUtil;
        this.dateParserFormatterUtil = new DateParserFormatterUtil(reflectionUtil.getDateFormat(),reflectionUtil.getDateTimeFormat());
        initCellValueMap();
        initValueSetterMap();
    }

    public Object getCellValue(Class<?> type, Cell cell) {
        try {
            var function = cellValueMap.get(getTypeName(type));
            if(function==null)
                throw new ExcelValidationException("Unsupported field type");
            return function.apply(cell);
        } catch (IllegalStateException | NumberFormatException e) {
            throw new ExcelValidationException(String.format("Invalid format in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex())));
        }
    }

    public void setCellValue(Class<?> type, Cell cell, Object value) {
        if(value==null)
            return;
        var function = cellValueSetterMap.get(getTypeName(type));
        if(function==null)
            throw new ExcelValidationException("Unsupported field type");
        function.accept(cell, value);
    }

    public void fillRowFromObject(Row row, T obj) {
        var fields = reflectionUtil.getFields();
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            setCellValue(field.type(), row.createCell(i),field.getValue(obj));
        }
    }

    public T createObjectFromRow(Row currentRow) {
        var fields = reflectionUtil.getFields();
        Object[] values = fields.stream()
                .map(field -> getCurrentCell(field.colIndex(), currentRow)
                        .map(cell -> getCellValue(field.type(),cell))
                        .orElse(null)).toArray();
        return reflectionUtil.getInstance(values);
    }
    public String[] getHeaders() {
        return reflectionUtil.getFields().stream().map(Field::title).toArray(String[]::new);
    }
    private String getTypeName(Class<?> type) {
        return type.isEnum() ? Enum.class.getSimpleName().toLowerCase() : type.getSimpleName().toLowerCase();
    }

    private void initCellValueMap() {
        cellValueMap.put(String.class.getSimpleName().toLowerCase(), Cell::getStringCellValue);
        cellValueMap.put(boolean.class.getSimpleName().toLowerCase(), Cell::getBooleanCellValue);
        cellValueMap.put(Enum.class.getSimpleName().toLowerCase(), Cell::getStringCellValue);
        cellValueMap.put(Integer.class.getSimpleName().toLowerCase(), cell -> (int) cell.getNumericCellValue());
        cellValueMap.put(int.class.getSimpleName().toLowerCase(), cell -> (int) cell.getNumericCellValue());
        cellValueMap.put(short.class.getSimpleName().toLowerCase(), cell -> (short) cell.getNumericCellValue());
        cellValueMap.put(long.class.getSimpleName().toLowerCase(), cell -> (long) cell.getNumericCellValue());
        cellValueMap.put(double.class.getSimpleName().toLowerCase(), Cell::getNumericCellValue);
        cellValueMap.put(LocalDate.class.getSimpleName().toLowerCase(), this::getAsLocalDate);
        cellValueMap.put(LocalDateTime.class.getSimpleName().toLowerCase(), this::getAsLocalDateTime);
        cellValueMap.put(ZonedDateTime.class.getSimpleName().toLowerCase(), this::getAsZonedDateTime);
        cellValueMap.put(Date.class.getSimpleName().toLowerCase(), this::getAsDate);
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

    private static String getInvalidCellDateMsg(Cell cell) {
        return String.format("Invalid or unsupported date format in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex()));
    }

    private Optional<Cell> getCurrentCell(int colIndex, Row currentRow) {
        return Optional.ofNullable(currentRow.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
    }

}
