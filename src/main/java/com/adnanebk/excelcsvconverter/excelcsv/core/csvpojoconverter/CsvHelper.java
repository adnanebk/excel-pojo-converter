package com.adnanebk.excelcsvconverter.excelcsv.core.csvpojoconverter;


import com.adnanebk.excelcsvconverter.excelcsv.core.ColumnDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.SheetValidationException;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


public class CsvHelper<T> {

    public static final char QUOTE_CHARACTER = ICSVWriter.NO_QUOTE_CHARACTER;
    public static final char ESCAPE_CHARACTER = '\t';
    public static final String LINE_END = "\n";
    public final String delimiter;
    private final String[] headers;
    private final ReflectionHelper<T> reflectionHelper;

    private CsvHelper(ReflectionHelper<T> reflectionHelper,String delimiter, String[] headers) {
        this.reflectionHelper = reflectionHelper;
        this.delimiter = delimiter;
        this.headers = headers;
    }

    public static <T> CsvHelper<T> create(Class<T> type, String delimiter) {
        var reflectionHelper = new ReflectionHelper<>(type);
        return new CsvHelper<>(reflectionHelper,delimiter, reflectionHelper.getHeaders().toArray(String[]::new));
    }

    public static <T> CsvHelper<T> create(Class<T> type, String delimiter, ColumnDefinition<?>... columnsDefinitions) {
        var headers = Arrays.stream(columnsDefinitions).map(ColumnDefinition::getTitle);
        var reflectionHelper = new ReflectionHelper<>(type, columnsDefinitions);
        return new CsvHelper<>(reflectionHelper, delimiter, headers.toArray(String[]::new));
    }

    public Stream<T> toStream(InputStream inputStream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        return br.lines().skip(1).map(this::convertToObject);
    }

    public ByteArrayInputStream toCsv(List<T> list) {
        StringWriter stringWriter = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(stringWriter, delimiter.charAt(0), QUOTE_CHARACTER, ESCAPE_CHARACTER, LINE_END)) {
            List<String[]> lines = new LinkedList<>();
            lines.add(headers);
            list.forEach(obj->lines.add(this.convertToLine(obj)));
            csvWriter.writeAll(lines);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new SheetValidationException(e.getMessage());
        }
    }

    private T convertToObject(String line) {
        String[] cellsValues = line.split(delimiter);
        var fields = reflectionHelper.getFields();
        T obj = reflectionHelper.createInstance();
        for (int i = 0; i < Math.min(cellsValues.length, fields.size()); i++) {
            var field = fields.get(i);
            String cellValue = cellsValues[i].replace(QUOTE_CHARACTER + "", "");
            try {
                if(!cellValue.isEmpty())
                    field.setValue(cellValue, obj);
            } catch (IllegalArgumentException e) {
                throw new SheetValidationException(String.format("Cannot convert the cell value %s to the field %s", cellValue,field.getName()));
            } catch (DateTimeException e) {
                throw new SheetValidationException(String.format("Invalid or unsupported date pattern for cell value : %s ,you should create a converter for the field %s", cellValue,field.getName()));
            }
        }
        return obj;
    }

    private String[] convertToLine(T obj) {
        return reflectionHelper.getFields().stream()
                .map(field -> field.getValue(obj).toString())
                .toArray(String[]::new);
    }

}