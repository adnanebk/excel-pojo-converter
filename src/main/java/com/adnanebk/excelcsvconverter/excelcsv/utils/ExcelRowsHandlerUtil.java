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
import java.util.Optional;

public class ExcelRowsHandlerUtil<T> {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final DateParserFormatterUtil dateParserFormatterUtil;
    private final ReflectionUtil<T> reflectionUtil;


    public ExcelRowsHandlerUtil(ReflectionUtil<T> reflectionUtil) {
        this.reflectionUtil = reflectionUtil;
        this.dateParserFormatterUtil = new DateParserFormatterUtil(reflectionUtil.getDatePattern(),reflectionUtil.getDateTimePattern());
    }

    public Object getCellValue(String typeName, Cell cell) {
        try {
            return switch (typeName) {
                case "string", "enum" -> cell.getStringCellValue();
                case "boolean" -> cell.getBooleanCellValue();
                case "integer", "int" -> (int) cell.getNumericCellValue();
                case "short" -> (short) cell.getNumericCellValue();
                case "long" -> (long) cell.getNumericCellValue();
                case "double" -> cell.getNumericCellValue();
                case "localdate" -> getAsLocalDate(cell);
                case "localdatetime" -> getAsLocalDateTime(cell);
                case "zoneddatetime" -> getAsZonedDateTime(cell);
                case "date" -> getAsDate(cell);
                default -> throw new ExcelValidationException("Unsupported field type "+typeName);
            };
        } catch (RuntimeException e) {
            throw new ExcelValidationException(String.format("Unexpected or Invalid cell value in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex())));
        }
    }

    public void setCellValue(Field<T> field, Cell cell, Object value) {
        if(value==null)
            return;
        switch (reflectionUtil.getFieldTypeName(field.type())) {
            case "string", "enum" -> cell.setCellValue(value.toString());
            case "double", "float", "integer", "int", "long", "short" ->
                    cell.setCellValue(Double.parseDouble(value.toString()));
            case "boolean" -> cell.setCellValue(Boolean.parseBoolean(value.toString()));
            case "localdate" -> cell.setCellValue(dateParserFormatterUtil.format((LocalDate) value));
            case "localdatetime" -> cell.setCellValue(dateParserFormatterUtil.format((LocalDateTime) value));
            case "zoneddatetime" -> cell.setCellValue(dateParserFormatterUtil.format((ZonedDateTime) value));
            case "date" -> cell.setCellValue(dateParserFormatterUtil.format((Date) value));
            default -> throw new ExcelValidationException("Unsupported field type");
        }
    }

    public void fillRowFromObject(Row row, T obj) {
        var fields = reflectionUtil.getFields();
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            setCellValue(field, row.createCell(i),field.getValue(obj));
        }
    }

    public T createObjectFromRow(Row currentRow) {
        var fields = reflectionUtil.getFields();
        Object[] values = fields.stream()
                .map(field -> getCurrentCell(field.colIndex(), currentRow)
                        .map(cell -> getCellValue(reflectionUtil.getFieldTypeName(field.type()),cell))
                        .orElse(null)).toArray();
        return reflectionUtil.createInstanceAndSetValues(values);
    }
    public String[] getHeaders() {
        return reflectionUtil.getFields().stream().map(Field::title).toArray(String[]::new);
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
