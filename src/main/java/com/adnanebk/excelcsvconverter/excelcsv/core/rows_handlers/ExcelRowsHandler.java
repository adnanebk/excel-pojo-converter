package com.adnanebk.excelcsvconverter.excelcsv.core.rows_handlers;

import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectedField;
import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.time.*;
import java.util.Date;
import java.util.Optional;

public class ExcelRowsHandler<T> {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final DateParserFormatter dateParserFormatter;
    private final ReflectionHelper<T> reflectionHelper;


    public ExcelRowsHandler(ReflectionHelper<T> reflectionHelper) {
        this.reflectionHelper = reflectionHelper;
        this.dateParserFormatter = reflectionHelper.getSheetInfo()
                .map(info->new DateParserFormatter(info.datePattern(),info.dateTimePattern()))
                .orElseGet(DateParserFormatter::new);
    }

    public void fillRowFromObject(Row row, T obj) {
        var fields = reflectionHelper.getFields();
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            setCellValue(field.getTypeName(), row.createCell(i),field.getValue(obj));
        }
    }

    public T convertToObject(Row currentRow) {
        T obj = reflectionHelper.createInstance();
        for (var field : reflectionHelper.getFields()) {
            field.setValue(getCellValue(field, currentRow), obj);
        }
        return obj;
    }

    private Object getCellValue(ReflectedField<?> reflectedField, Row row) {
        return  this.getCurrentCell(reflectedField.getCellIndex(),row).map(cell -> {
            try {
                return switch (reflectedField.getTypeName()) {
                    case "string", "enum", "boolean" -> cell.getStringCellValue();
                    case "integer", "int" -> (int) cell.getNumericCellValue();
                    case "short" -> (short) cell.getNumericCellValue();
                    case "long" -> (long) cell.getNumericCellValue();
                    case "double" -> cell.getNumericCellValue();
                    case "localdate" -> getAsLocalDate(cell);
                    case "localdatetime" -> getAsLocalDateTime(cell);
                    case "zoneddatetime" -> getAsZonedDateTime(cell);
                    case "date" -> getAsDate(cell);
                    default -> throw new ExcelValidationException("Unsupported field typeName " + reflectedField.getTypeName());
                };
            } catch (IllegalStateException | NumberFormatException e) {
                throw new ExcelValidationException(String.format("Unexpected or Invalid cell value in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex())));
            } catch (DateTimeException e) {
                throw new ExcelValidationException(String.format("Invalid or unsupported date pattern in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex())));
            }
        }).orElse(null);
    }

    private void setCellValue(String fieldType, Cell cell, Object fieldValue) {
        if(fieldValue==null)
            return;
        switch (fieldType) {
            case "string","enum","boolean" -> cell.setCellValue(fieldValue.toString());
            case "double", "float", "integer", "int", "long", "short" -> cell.setCellValue(Double.parseDouble(fieldValue.toString()));
            case "localdate" -> cell.setCellValue(dateParserFormatter.format((LocalDate) fieldValue));
            case "localdatetime" -> cell.setCellValue(dateParserFormatter.format((LocalDateTime) fieldValue));
            case "zoneddatetime" -> cell.setCellValue(dateParserFormatter.format((ZonedDateTime) fieldValue));
            case "date" -> cell.setCellValue(dateParserFormatter.format((Date) fieldValue));
            default -> throw new ExcelValidationException("Unsupported field typeName");
        }
    }

    private Date getAsDate(Cell cell) {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getDateCellValue();
            return dateParserFormatter.parseToDate(cell.getStringCellValue());
    }

    private LocalDate getAsLocalDate(Cell cell) {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getLocalDateTimeCellValue().toLocalDate();
            return dateParserFormatter.parseToLocalDate(cell.getStringCellValue());

    }

    private LocalDateTime getAsLocalDateTime(Cell cell) {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getLocalDateTimeCellValue();
            return dateParserFormatter.parseToLocalDateTime(cell.getStringCellValue());
    }

    private ZonedDateTime getAsZonedDateTime(Cell cell) {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault());
            return dateParserFormatter.parseToZonedDateTime(cell.getStringCellValue());
    }

    private Optional<Cell> getCurrentCell(int colIndex, Row currentRow) {
        return Optional.ofNullable(currentRow.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
    }

}
