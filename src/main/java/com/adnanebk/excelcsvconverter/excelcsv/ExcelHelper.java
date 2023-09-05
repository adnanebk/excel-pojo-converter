package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.models.AnnotationType;
import com.adnanebk.excelcsvconverter.excelcsv.models.Field;
import com.adnanebk.excelcsvconverter.excelcsv.utils.DateParserUtil;
import com.adnanebk.excelcsvconverter.excelcsv.utils.ExcelCellHandlerUtil;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelFileException;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import com.adnanebk.excelcsvconverter.excelcsv.utils.ReflectionUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;


public class ExcelHelper<T> {

    private static final String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final String sheetName;

    private final ReflectionUtil<T> reflectionUtil;
    private final ExcelCellHandlerUtil<T> cellHandlerUtil;

    private ExcelHelper(String fineName, ReflectionUtil<T> reflectionUtil, ExcelCellHandlerUtil<T> cellHandlerUtil) {
        this.reflectionUtil = reflectionUtil;
        this.cellHandlerUtil = cellHandlerUtil;
        sheetName = fineName + "-" + LocalDate.now();
    }

    public static <T> ExcelHelper<T> create(Class<T> type, AnnotationType annotationType) {
        var reflectionUtil = new ReflectionUtil<>(type, annotationType);
        var tDateParserUtil = new DateParserUtil<>(reflectionUtil);
        var cellHandlerUtil = new ExcelCellHandlerUtil<>(tDateParserUtil);
        return new ExcelHelper<>(type.getSimpleName(), reflectionUtil, cellHandlerUtil);
    }

    public static <T> ExcelHelper<T> create(Class<T> type) {
        return create(type, AnnotationType.FIELD);
    }

    public List<T> toList(MultipartFile file) {
        if (!hasExcelFormat(file))
            throw new ExcelFileException("Only excel formats are valid");
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .filter(this::hasAnyCell)
                    .skip(1)
                    .map(this::rowToObject)
                    .toList();

        } catch (IOException e) {
            throw new ExcelFileException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public ByteArrayInputStream toExcel(List<T> list) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            createHeaders(sheet, workbook, reflectionUtil.getFields().stream().map(Field::title).toArray(String[]::new));
            for (int i = 1; i < list.size(); i++) {
                fillRowFromObject(sheet.createRow(i), list.get(i));
            }
            autoSizeColumns(sheet);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new ExcelValidationException("fail to import data to Excel file: ");
        }
    }

    private T rowToObject(Row currentRow) {
        List<Field> fields = reflectionUtil.getFields();
        Object[] values = IntStream.range(0, fields.size())
                .mapToObj(index -> getCurrentCell(index, currentRow)
                        .map(cell -> getCellValue(cell, fields.get(index).type()))
                        .orElse(null)
                ).toArray();
        return reflectionUtil.getInstance(values);

    }

    private Object getCellValue(Cell cell, Class<?> type) {
        return cellHandlerUtil.getCellValue(type, cell);
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < reflectionUtil.getFields().size(); i++)
            sheet.autoSizeColumn(i);
    }

    private void fillRowFromObject(Row row, T obj) {
        List<Field> fields = reflectionUtil.getFields();
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Object fieldValue = reflectionUtil.getFieldValue(obj, i);
            cellHandlerUtil.setCellValue(field.type(), row.createCell(i), fieldValue);
        }

    }


    private boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    private void createHeaders(Sheet sheet, Workbook workbook, String[] headers) {

        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        headerStyle.setFont(font);
        for (int colIdx = 0; colIdx < headers.length; colIdx++) {
            Cell cell = headerRow.createCell(colIdx);
            cell.setCellValue(headers[colIdx]);
            cell.setCellStyle(headerStyle);
        }
    }

    private boolean hasAnyCell(Row currentRow) {
        return currentRow.getPhysicalNumberOfCells() > 0;
    }


    private Optional<Cell> getCurrentCell(int colIndex, Row currentRow) {
        return Optional.ofNullable(currentRow.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
    }


}