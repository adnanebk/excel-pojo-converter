package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.ExcelHelper;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelFileException;
import com.adnanebk.excelcsvconverter.models.Category;
import com.adnanebk.excelcsvconverter.models.Product;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CsvHelperTest {
    private CsvHelper<Product> csvHelper;

    @BeforeEach
    void setUp() {
        csvHelper = CsvHelper.create(Product.class, ";");
    }


    @Test
    @Order(1)
    void toList_withValidCsvFile_shouldReturnCorrectProductList() throws IOException {
        // Read the file as an InputStream
        InputStream inputStream = new ClassPathResource("products.csv").getInputStream();

        // Create a MockMultipartFile
        MultipartFile multipartFile = new MockMultipartFile("file", "generatedCsv.csv", "text/csv", inputStream);
        List<Product> result = csvHelper.toList(multipartFile);

        // Assuming you know the expected size of the list
        assertEquals(19, result.size());

        // Add more assertions based on the actual data you expect

    }

    @Test
    @Order(2)
    void toList_withInvalidCsvFile_shouldThrowException() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "invalid.csv",
                getClass().getClassLoader().getResourceAsStream("invalid.csv")
        );

        assertThrows(ExcelFileException.class, () -> csvHelper.toList(file));
    }
}