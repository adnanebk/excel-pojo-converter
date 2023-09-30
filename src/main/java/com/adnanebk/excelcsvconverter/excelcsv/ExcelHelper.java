package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import com.adnanebk.excelcsvconverter.excelcsv.utils.ExcelRowsHandlerUtil;
import com.adnanebk.excelcsvconverter.excelcsv.utils.ReflectionUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class ExcelHelper<T> {

    private final ExcelRowsHandlerUtil<T> rowsHandlerUtil;
    private final String[] headers;

    private ExcelHelper(ExcelRowsHandlerUtil<T> rowsHandlerUtil, String[] headers) {
        this.rowsHandlerUtil = rowsHandlerUtil;
        this.headers=headers==null? rowsHandlerUtil.getHeaders():headers;
    }

    public static <T> ExcelHelper<T> create(Class<T> type) {
        var rowsHandlerUtil = new ExcelRowsHandlerUtil<>(new ReflectionUtil<>(type));
        return new ExcelHelper<>(rowsHandlerUtil,null);
    }
    public static <T> ExcelHelper<T> create(Class<T> type,String[] headers) {
        var rowsHandlerUtil = new ExcelRowsHandlerUtil<>(new ReflectionUtil<>(type));
        return new ExcelHelper<>(rowsHandlerUtil,headers);
    }


    public Stream<T> toStream(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .filter(this::hasAnyCell)
                    .skip(1)
                    .map(rowsHandlerUtil::createObjectFromRow);
        }
    }

    public ByteArrayInputStream toExcel(List<T> list) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            createHeaders(sheet, workbook);
            for (int i = 0; i < list.size(); i++) {
                this.rowsHandlerUtil.fillRowFromObject(sheet.createRow(i+1), list.get(i));
            }
            autoSizeColumns(sheet);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new ExcelValidationException("fail to import data to Excel file: ");
        }
    }

    private void createHeaders(Sheet sheet, Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        headerStyle.setFont(font);
        Row headerRow = sheet.createRow(0);
        for (int colIdx = 0; colIdx < headers.length; colIdx++) {
            Cell cell = headerRow.createCell(colIdx);
            cell.setCellValue(headers[colIdx]);
            cell.setCellStyle(headerStyle);
        }
    }
    private boolean hasAnyCell(Row currentRow) {
        return currentRow.getPhysicalNumberOfCells() > 0;
    }
    public void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < sheet.getLastRowNum(); i++)
            sheet.autoSizeColumn(i);
    }
}