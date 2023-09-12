package com.adnanebk.excelcsvconverter.excelcsv;


import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelFileException;
import com.adnanebk.excelcsvconverter.excelcsv.utils.CsvRowsHandlerUtil;
import com.adnanebk.excelcsvconverter.excelcsv.utils.ReflectionUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;


public class CsvHelper<T> {

    private static final String TYPE = "text/csv";
    public  final String delimiter;
    private final CsvRowsHandlerUtil<T> cellHandlerUtil;

    private CsvHelper(CsvRowsHandlerUtil<T> cellHandlerUtil, String delimiter) {
        this.cellHandlerUtil = cellHandlerUtil;
        this.delimiter =delimiter;
    }

    public static <T> CsvHelper<T> create(Class<T> type,String delimiter) {
        return new CsvHelper<>( new CsvRowsHandlerUtil<>(new ReflectionUtil<>(type)),delimiter);
    }

    public Stream<T> toStream(MultipartFile file) {
        if (!hasCsvFormat(file))
            throw new ExcelFileException("Only csv formats are valid");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            return br.lines().skip(1)
                    .map(row -> this.cellHandlerUtil.createObjectFromCells(row,delimiter));
        } catch (IOException e) {
            throw new ExcelFileException("fail to parse Excel file: " + e.getMessage());
        }
    }

    private boolean hasCsvFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

}