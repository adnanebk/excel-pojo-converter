package com.adnanebk.excelcsvconverter.excelcsv;


import com.adnanebk.excelcsvconverter.excelcsv.utils.CsvRowsHandlerUtil;
import com.adnanebk.excelcsvconverter.excelcsv.utils.ReflectionUtil;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


public class CsvHelper<T> {

    public  final String delimiter;
    private final String[] headers;
    private final CsvRowsHandlerUtil<T> rowsHandlerUtil;

    private CsvHelper(CsvRowsHandlerUtil<T> rowsHandlerUtil, String delimiter, String[] headers) {
        this.rowsHandlerUtil = rowsHandlerUtil;
        this.delimiter =delimiter;
        this.headers=headers==null?rowsHandlerUtil.getHeaders():headers;

    }

    public static <T> CsvHelper<T> create(Class<T> type,String delimiter) {
        return new CsvHelper<>( new CsvRowsHandlerUtil<>(new ReflectionUtil<>(type)),delimiter,null);
    }
    public static <T> CsvHelper<T> create(Class<T> type,String delimiter,String[] headers) {
        return new CsvHelper<>( new CsvRowsHandlerUtil<>(new ReflectionUtil<>(type)),delimiter,headers);
    }

    public Stream<T> toStream(InputStream inputStream) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            return br.lines().skip(1)
                    .map(row -> this.rowsHandlerUtil.createObjectFromCells(row,delimiter));
    }

    public ByteArrayInputStream toCsv(List<T> list) throws IOException {
        StringWriter stringWriter=new StringWriter();
        try(CSVWriter csvWriter = new CSVWriter(stringWriter, delimiter.charAt(0), ICSVWriter.NO_QUOTE_CHARACTER,'\t',"\n")) {
            List<String[]> data = new LinkedList<>();
            data.add(headers);
            for (T obj : list) {
                data.add(rowsHandlerUtil.getFieldValuesAsStrings(obj));
            }
            csvWriter.writeAll(data);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        }
    }


}