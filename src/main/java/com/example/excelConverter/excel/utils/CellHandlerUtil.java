package com.example.excelConverter.excel.utils;

import com.example.excelConverter.excel.exceptions.ReflectionException;
import com.example.excelConverter.excel.models.Field;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class CellHandlerUtil<T> {
  private final ReflectionUtil<T> reflectionUtil;
    private final SimpleDateFormat dateFormatter;
    private final DateTimeFormatter localedDateFormatter;
    private final DateTimeFormatter localedDateTimeFormatter;
    private final DateTimeFormatter zonedDateTimeFormatter;
    private final Map<String, BiFunction<Cell, Class<?>, Object>> cellValueMap = new HashMap<>();
    private final Map<String, BiConsumer<Cell, Object>> cellValueSetterMap = new HashMap<>();


    public CellHandlerUtil(ReflectionUtil<T> reflectionUtil) {
        this.reflectionUtil = reflectionUtil;
        dateFormatter = this.reflectionUtil.dateTimeFormat().map(SimpleDateFormat::new).orElse(new SimpleDateFormat());
        localedDateFormatter =this.reflectionUtil.dateFormat().map(DateTimeFormatter::ofPattern).orElse(DateTimeFormatter.ISO_LOCAL_DATE);
        localedDateTimeFormatter =this.reflectionUtil.dateTimeFormat().map(DateTimeFormatter::ofPattern).orElse(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        zonedDateTimeFormatter =DateTimeFormatter.ISO_ZONED_DATE_TIME;
        initCellValueMap();
        initValueSetterMap();
    }

    public  Object getCellValue(Field field, Cell cell) {
      return  cellValueMap.get(getTypeName(field)).apply(cell,field.type());
    }
    public void setCellValue(Field field, Cell cell, Object value){
        cellValueSetterMap.get(getTypeName(field)).accept(cell,value);

    }

    private String getTypeName(Field field) {
        return field.type().isEnum() ? Enum.class.getSimpleName().toLowerCase() : field.type().getSimpleName().toLowerCase();
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
        cellValueMap.put(ZonedDateTime.class.getSimpleName().toLowerCase(), (cell, fieldType) -> ZonedDateTime.parse(cell.getStringCellValue(), zonedDateTimeFormatter));
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
        cellValueSetterMap.put(LocalDate.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(localedDateFormatter.format((LocalDate) value)));
        cellValueSetterMap.put(LocalDateTime.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(localedDateTimeFormatter.format((LocalDateTime) value)));
        cellValueSetterMap.put(ZonedDateTime.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(zonedDateTimeFormatter.format((ZonedDateTime) value)));
        cellValueSetterMap.put(Date.class.getSimpleName().toLowerCase(), (cell, value) -> cell.setCellValue(dateFormatter.format((Date)value)));
    }
    private Date getAsDate(Cell cell) {
        try {
            if(cell.getCellType().equals(CellType.NUMERIC))
                cell.setCellValue(dateFormatter.format(DateUtil.getLocalDateTime(cell.getNumericCellValue())));
            return dateFormatter.parse(cell.getStringCellValue());
        } catch (ParseException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    private LocalDate getAsLocalDate(Cell cell) {
        if(cell.getCellType().equals(CellType.NUMERIC))
            cell.setCellValue(localedDateFormatter.format(DateUtil.getLocalDateTime(cell.getNumericCellValue())));
        return LocalDate.parse(cell.getStringCellValue(), localedDateFormatter);
    }
    private LocalDateTime getAsLocalDateTime(Cell cell) {
        if(cell.getCellType().equals(CellType.NUMERIC))
            cell.setCellValue(localedDateTimeFormatter.format(DateUtil.getLocalDateTime(cell.getNumericCellValue())));
        return LocalDateTime.parse(cell.getStringCellValue(),localedDateTimeFormatter);
    }
}
