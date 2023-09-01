package com.example.excelConverter.excel.utils;

import com.example.excelConverter.excel.exceptions.ReflectionException;
import com.example.excelConverter.excel.models.Field;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class CellHandlerUtil {
  private final ReflectionUtil<?> reflectionUtil;
    private final Map<String, BiFunction<Cell, Class<?>, Object>> cellValueMap = new HashMap<>();
    private final Map<String, BiConsumer<Cell, Object>> cellValueSetterMap = new HashMap<>();


    public CellHandlerUtil(ReflectionUtil<?> reflectionUtil) {
        this.reflectionUtil = reflectionUtil;
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
        return field.type().isEnum() ? Enum.class.getName() : field.type().getName();
    }

    private void initCellValueMap() {
        cellValueMap.put(String.class.getName(), (cell, fieldType) -> cell.getStringCellValue());
        cellValueMap.put(Boolean.class.getName(), (cell, fieldType) -> cell.getBooleanCellValue());
        cellValueMap.put(boolean.class.getName(), (cell, fieldType) -> cell.getBooleanCellValue());
        cellValueMap.put(Enum.class.getName(), (cell, fieldType) -> Enum.valueOf(fieldType.asSubclass(Enum.class), cell.getStringCellValue()));
        cellValueMap.put(Integer.class.getName(), (cell, fieldType) -> (int) cell.getNumericCellValue());
        cellValueMap.put(int.class.getName(), (cell, fieldType) -> (int) cell.getNumericCellValue());
        cellValueMap.put(Short.class.getName(), (cell, fieldType) -> (short) cell.getNumericCellValue());
        cellValueMap.put(short.class.getName(), (cell, fieldType) -> (short) cell.getNumericCellValue());
        cellValueMap.put(Long.class.getName(), (cell, fieldType) -> (long) cell.getNumericCellValue());
        cellValueMap.put(long.class.getName(), (cell, fieldType) -> (long) cell.getNumericCellValue());
        cellValueMap.put(Double.class.getName(), (cell, fieldType) -> cell.getNumericCellValue());
        cellValueMap.put(double.class.getName(), (cell, fieldType) -> cell.getNumericCellValue());
        cellValueMap.put(LocalDate.class.getName(), (cell, fieldType) -> getAsLocalDate(cell));
        cellValueMap.put(LocalDateTime.class.getName(), (cell, fieldType) -> getAsLocalDateTime(cell));
        cellValueMap.put(ZonedDateTime.class.getName(), (cell, fieldType) -> ZonedDateTime.parse(cell.getStringCellValue(), reflectionUtil.zonedDateTimeFormatter));
        cellValueMap.put(Date.class.getName(), (cell, fieldType) -> getAsDate(cell));
    }
    private void initValueSetterMap() {
        cellValueSetterMap.put(String.class.getName(), (cell, value) -> cell.setCellValue(value.toString()));
        cellValueSetterMap.put(Double.class.getName(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(double.class.getName(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(Integer.class.getName(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(int.class.getName(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(Long.class.getName(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(long.class.getName(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(short.class.getName(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(Short.class.getName(), (cell, value) -> cell.setCellValue(Double.parseDouble(value.toString())));
        cellValueSetterMap.put(Boolean.class.getName(), (cell, value) -> cell.setCellValue((boolean) value));
        cellValueSetterMap.put(boolean.class.getName(), (cell, value) -> cell.setCellValue((boolean) value));
        cellValueSetterMap.put(Enum.class.getName(), (cell, value) -> cell.setCellValue(value.toString()));
        cellValueSetterMap.put(LocalDate.class.getName(), (cell, value) -> cell.setCellValue(reflectionUtil.localedDateFormatter.format((LocalDate) value)));
        cellValueSetterMap.put(LocalDateTime.class.getName(), (cell, value) -> cell.setCellValue(reflectionUtil.localedDateTimeFormatter.format((LocalDateTime) value)));
        cellValueSetterMap.put(ZonedDateTime.class.getName(), (cell, value) -> cell.setCellValue(reflectionUtil.zonedDateTimeFormatter.format((ZonedDateTime) value)));
        cellValueSetterMap.put(Date.class.getName(), (cell, value) -> cell.setCellValue(reflectionUtil.dateFormatter.format((Date)value)));
    }
    private Date getAsDate(Cell cell) {
        try {
            if(cell.getCellType().equals(CellType.NUMERIC))
                cell.setCellValue(reflectionUtil.dateFormatter.format(DateUtil.getLocalDateTime(cell.getNumericCellValue())));
            return reflectionUtil.dateFormatter.parse(cell.getStringCellValue());
        } catch (ParseException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    private LocalDate getAsLocalDate(Cell cell) {
        if(cell.getCellType().equals(CellType.NUMERIC))
            cell.setCellValue(reflectionUtil.localedDateFormatter.format(DateUtil.getLocalDateTime(cell.getNumericCellValue())));
        return LocalDate.parse(cell.getStringCellValue(), reflectionUtil.localedDateFormatter);
    }
    private LocalDateTime getAsLocalDateTime(Cell cell) {
        if(cell.getCellType().equals(CellType.NUMERIC))
            cell.setCellValue(reflectionUtil.localedDateTimeFormatter.format(DateUtil.getLocalDateTime(cell.getNumericCellValue())));
        return LocalDateTime.parse(cell.getStringCellValue(), reflectionUtil.localedDateTimeFormatter);
    }
}
