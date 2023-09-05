package com.adnanebk.excelcsvconverter.excelcsv;


import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelFileException;
import com.adnanebk.excelcsvconverter.excelcsv.models.AnnotationType;
import com.adnanebk.excelcsvconverter.excelcsv.models.Field;
import com.adnanebk.excelcsvconverter.excelcsv.utils.CsvCellHandlerUtil;
import com.adnanebk.excelcsvconverter.excelcsv.utils.DateParserUtil;
import com.adnanebk.excelcsvconverter.excelcsv.utils.ReflectionUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class CsvHelper<T> {

    private static final String TYPE = "text/csv";
    private final ReflectionUtil<T> reflectionUtil;
    private final CsvCellHandlerUtil<T> cellHandlerUtil;

    public CsvHelper(ReflectionUtil<T> reflectionUtil, CsvCellHandlerUtil<T> cellHandlerUtil) {
        this.reflectionUtil = reflectionUtil;
        this.cellHandlerUtil = cellHandlerUtil;
    }

    public static <T> CsvHelper<T> create(Class<T> type, AnnotationType annotationType) {
        var reflectionUtil = new ReflectionUtil<>(type, annotationType);
        var tDateParserUtil = new DateParserUtil<>(reflectionUtil);
        var cellHandlerUtil = new CsvCellHandlerUtil<>(tDateParserUtil);
        return new CsvHelper<>(reflectionUtil, cellHandlerUtil);
    }

    public static <T> CsvHelper<T> create(Class<T> type) {
        return create(type, AnnotationType.FIELD);
    }

    public List<T> csvToList(MultipartFile file) {
        if (!hasCsvFormat(file))
            throw new ExcelFileException("Only csv formats are valid");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return br.lines().parallel().skip(1).map(row -> {
                String[] columns = row.split(";");
                Object[] values = new Object[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    String cellValue = columns[i];
                    Field field = reflectionUtil.getFields().get(i);
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