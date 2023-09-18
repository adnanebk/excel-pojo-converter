package com.adnanebk.excelcsvconverter.excelcsv;


import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelFileException;
import com.adnanebk.excelcsvconverter.excelcsv.utils.CsvRowsHandlerUtil;
import com.adnanebk.excelcsvconverter.excelcsv.utils.ReflectionUtil;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
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

    public ByteArrayInputStream toCsv(List<T> list) throws IOException {
        StringWriter stringWriter=new StringWriter();
        try(CSVWriter csvWriter = new CSVWriter(stringWriter, delimiter.charAt(0), ICSVWriter.NO_QUOTE_CHARACTER,'\t',"\n")) {
            List<String[]> data = new LinkedList<>();
            data.add(cellHandlerUtil.getHeaders());
            for (T obj : list) {
                data.add(cellHandlerUtil.convertObjectToStringsOfColumns(obj));
            }
            csvWriter.writeAll(data);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private boolean hasCsvFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

}