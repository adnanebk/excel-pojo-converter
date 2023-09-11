package com.adnanebk.excelcsvconverter.excelcsv;


import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelFileException;
import com.adnanebk.excelcsvconverter.excelcsv.utils.CsvCellHandlerUtil;
import com.adnanebk.excelcsvconverter.excelcsv.utils.DateParserFormatterUtil;
import com.adnanebk.excelcsvconverter.excelcsv.utils.ReflectionUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class CsvHelper<T> {

    private static final String TYPE = "text/csv";
    public  final String delimiter;
    private final ReflectionUtil<T> reflectionUtil;
    private final CsvCellHandlerUtil<T> cellHandlerUtil;

    private CsvHelper(ReflectionUtil<T> reflectionUtil, CsvCellHandlerUtil<T> cellHandlerUtil, String delimiter) {
        this.reflectionUtil = reflectionUtil;
        this.cellHandlerUtil = cellHandlerUtil;
        this.delimiter =delimiter;
    }

    public static <T> CsvHelper<T> create(Class<T> type,String delimiter) {
        var reflectionUtil = new ReflectionUtil<>(type);
        var tDateParserUtil = new DateParserFormatterUtil<>(reflectionUtil);
        var cellHandlerUtil = new CsvCellHandlerUtil<>(tDateParserUtil);

        return new CsvHelper<>(reflectionUtil, cellHandlerUtil,delimiter);
    }

    public List<T> toList(MultipartFile file) {
        if (!hasCsvFormat(file))
            throw new ExcelFileException("Only csv formats are valid");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return br.lines().skip(1).map(row -> {
                String[] columns = row.split(delimiter);
                Object[] values = new Object[columns.length];
                var fields = reflectionUtil.getFields();
                for (int i = 0; i < Math.min(columns.length,fields.size()); i++) {
                    var field = fields.get(i);
                    String cellValue = columns[field.colIndex()];
                    values[i] = cellHandlerUtil.getCellValue(cellValue, field.type());
                }
                return reflectionUtil.getInstance(values);
            }).toList();
        } catch (IOException e) {
            throw new ExcelFileException("fail to parse Excel file: " + e.getMessage());
        }
    }

    private boolean hasCsvFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

}