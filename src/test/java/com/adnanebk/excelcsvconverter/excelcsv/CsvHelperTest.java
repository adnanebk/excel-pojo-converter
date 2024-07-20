package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.core.heplers.CsvHelper;
import com.adnanebk.excelcsvconverter.excelcsv.models.Category;
import com.adnanebk.excelcsvconverter.excelcsv.models.Product;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CsvHelperTest {
    private CsvHelper<Product> csvHelper;

    @BeforeEach
    void setUp() {
        csvHelper = CsvHelper.create(Product.class, ";");
    }

    private static List<Product> getProducts() {
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("Product A", 100, 90.5, 80.0, true, false, 50, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B, LocalDateTime.now()));
        productList.add(new Product("Product B", 200, 180.75, 150.0, true, true, 30, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B,LocalDateTime.now()));
        return productList;
    }
    @Test
    @Order(0)
    void toCsv_withValidProductData_shouldReturnCorrectExcel() {
        List<Product> productList = getProducts();
        String destinationPath = "src/test/resources/products.csv";
        File file =new File(destinationPath);
        try (ByteArrayInputStream byteArrayInputStream = csvHelper.toCsv(productList);
                OutputStream outputStream = new FileOutputStream(file)) {
            byteArrayInputStream.transferTo(outputStream);
            BufferedReader reader = new BufferedReader(new FileReader(destinationPath));
            List<String> lines = reader.lines().toList();
            assertNotNull(lines);
            assertEquals(3, lines.size());
            assertTrue(lines.get(0).contains("Name;Price;Promotion price;Min price;Active;Expired;Units in stock;Created date;Updated date;Zoned date time;Category;Local date time"));
            assertTrue(lines.get(1).contains("Product A;100;90.5;80.0;true;No;50"));
            } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    @Test
    @Order(1)
    void toList_withValidCsvFile_shouldReturnCorrectProductList() throws IOException {
        String destinationPath = "src/test/resources/products.csv";
        // Read the file as an InputStream
        try (InputStream inputStream = Files.newInputStream(new File(destinationPath).toPath())) {
            // Create a MockMultipartFile
            List<Product> result = csvHelper.toStream(inputStream).toList();

            assertEquals("Product A", result.get(0).getName());
            assertEquals(100, result.get(0).getPrice());
            assertEquals(90.5, result.get(0).getPromoPrice()); // Delta for double comparison
            assertTrue(result.get(0).isActive());
            assertFalse(result.get(0).getExpired());
            assertEquals(50, result.get(0).getUnitsInStock());
            assertEquals("Product B", result.get(1).getName());
            assertEquals(200, result.get(1).getPrice());
            assertEquals(180.75, result.get(1).getPromoPrice()); // Delta for double comparison
            assertTrue(result.get(1).isActive());
            assertTrue(result.get(1).getExpired());
            assertEquals(30, result.get(1).getUnitsInStock());
            assertNotNull(result.get(1).getCreatedDate()); // Assuming it's not null in the Excel file
            assertTrue(LocalDate.now().isEqual(result.get(1).getUpdatedDate())); // Assuming it's not null in the Excel file
            assertNotNull(result.get(1).getZonedDateTime()); // Assuming it's not null in the Excel file
            assertSame(Category.B, result.get(1).getCategory()); // Assuming it's not null in the Excel file
        }

    }
}