package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.models.Product;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        List<Product> result = csvHelper.toStream(multipartFile.getInputStream()).toList();

        // Assuming you know the expected size of the list
        assertEquals(19, result.size());
        assertEquals("P1", result.get(0).getName());
        assertEquals(200, result.get(0).getPrice());

        // Add more assertions based on the actual data you expect


    }
}