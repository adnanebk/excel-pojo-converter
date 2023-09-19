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

    private static final String FILE_TYPE = "text/csv";
    public  final String delimiter;
    private final CsvRowsHandlerUtil<T> rowsHandlerUtil;

    private CsvHelper(CsvRowsHandlerUtil<T> rowsHandlerUtil, String delimiter) {
        this.rowsHandlerUtil = rowsHandlerUtil;
        this.delimiter =delimiter;
    }

    public static <T> CsvHelper<T> create(Class<T> type,String delimiter) {
        return new CsvHelper<>( new CsvRowsHandlerUtil<>(new ReflectionUtil<>(type)),delimiter);
    }

    public Stream<T> toStream(File file){
        try {
            return toStream(new FileInputStream(file));
        } catch (IOException e) {
            throw new ExcelFileException("fail to parse Csv file: " + e.getMessage());
        }
    }

    public Stream<T> toStream(MultipartFile file){
      try {
        if (!hasCsvFormat(file))
            throw new ExcelFileException("Only csv formats are valid");
        return toStream(file.getInputStream());
    } catch (IOException e) {
        throw new ExcelFileException("fail to parse Csv file: " + e.getMessage());
    }
    }

    private Stream<T> toStream(InputStream inputStream) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            return br.lines().skip(1)
                    .map(row -> this.rowsHandlerUtil.createObjectFromCells(row,delimiter));
    }

    public ByteArrayInputStream toCsv(List<T> list) throws IOException {
        StringWriter stringWriter=new StringWriter();
        try(CSVWriter csvWriter = new CSVWriter(stringWriter, delimiter.charAt(0), ICSVWriter.NO_QUOTE_CHARACTER,'\t',"\n")) {
            List<String[]> data = new LinkedList<>();
            data.add(rowsHandlerUtil.getHeaders());
            for (T obj : list) {
                data.add(rowsHandlerUtil.convertObjectToStringColumns(obj));
            }
            csvWriter.writeAll(data);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private boolean hasCsvFormat(MultipartFile file) {
        return FILE_TYPE.equals(file.getContentType());
    }

}