package com.adnanebk.excelcsvconverter.excelcsv.core.excelpojoconverter;

import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectedField;
import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.SheetValidationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.time.*;
import java.util.Date;

public class ExcelRowsHandler<T> {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final DateParserFormatter dateParserFormatter;
    private final ReflectionHelper<T> reflectionHelper;


    public ExcelRowsHandler(ReflectionHelper<T> reflectionHelper) {
        this.reflectionHelper = reflectionHelper;
        this.dateParserFormatter = reflectionHelper.getDateParserFormatter();
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
        var cell =  this.getCurrentCell(reflectedField.getCellIndex(),row);
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
                    default -> throw new SheetValidationException("Unsupported field typeName " + reflectedField.getTypeName());
                };
            } catch (IllegalStateException | NumberFormatException e) {
                throw new SheetValidationException(String.format("Unexpected or Invalid cell value in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex())));
            } catch (DateTimeException e) {
                throw new SheetValidationException(String.format("Invalid or unsupported date pattern in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex())));
            }
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
            default -> throw new SheetValidationException("Unsupported field typeName");
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

    private Cell getCurrentCell(int colIndex, Row currentRow) {
        return currentRow.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
    }

}
