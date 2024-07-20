package com.adnanebk.excelcsvconverter.excelcsv.core.heplers;


import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.core.rows_handlers.CsvRowsHandler;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.SheetValidationException;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


public class CsvHelper<T> {

    public static final char QUOTE_CHARACTER = ICSVWriter.NO_QUOTE_CHARACTER;
    public static final char ESCAPE_CHARACTER = '\t';
    public static final String LINE_END = "\n";
    public  final String delimiter;
    private final String[] headers;
    private final CsvRowsHandler<T> rowsHandler;

    private CsvHelper(CsvRowsHandler<T> rowsHandler, String delimiter, String[] headers) {
        this.rowsHandler = rowsHandler;
        this.delimiter =delimiter;
        this.headers= headers;
    }

    public static <T> CsvHelper<T> create(Class<T> type,String delimiter) {
        var reflectionHelper = new ReflectionHelper<>(type);
        var rowsHandler = new CsvRowsHandler<>(reflectionHelper);
        return new CsvHelper<>(rowsHandler,delimiter,reflectionHelper.getHeaders().toArray(String[]::new));
    }

        public static <T> CsvHelper<T> create(Class<T> type,String delimiter, ColumnDefinition... columnsDefinitions) {
        var headers = Arrays.stream(columnsDefinitions).map(ColumnDefinition::getTitle);
        var reflectionHelper = new ReflectionHelper<>(type, columnsDefinitions);
        var rowsHandler = new CsvRowsHandler<>(reflectionHelper);
        return new CsvHelper<>(rowsHandler,delimiter,headers.toArray(String[]::new));
    }

    public Stream<T> toStream(InputStream inputStream) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            return br.lines().skip(1)
                    .map(row -> this.rowsHandler.convertToObject(row,delimiter,QUOTE_CHARACTER));
    }

    public ByteArrayInputStream toCsv(List<T> list) {
        StringWriter stringWriter=new StringWriter();
        try(CSVWriter csvWriter = new CSVWriter(stringWriter, delimiter.charAt(0), QUOTE_CHARACTER, ESCAPE_CHARACTER, LINE_END)) {
            List<String[]> data = new LinkedList<>();
            data.add(headers);
            for (T obj : list) {
                data.add(rowsHandler.convertFieldValuesToStrings(obj));
            }
            csvWriter.writeAll(data);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new SheetValidationException(e.getMessage());
        }
    }


}