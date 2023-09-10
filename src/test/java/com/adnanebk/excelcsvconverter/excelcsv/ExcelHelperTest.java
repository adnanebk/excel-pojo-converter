package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelFileException;
import com.adnanebk.excelcsvconverter.excelcsv.models.AnnotationType;
import com.adnanebk.excelcsvconverter.models.Category;
import com.adnanebk.excelcsvconverter.models.Product;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExcelHelperTest {
    private final ExcelHelper<Product> excelHelper = ExcelHelper.create(Product.class);

    @BeforeEach
    void setUp() {
    }
    private static Stream<Arguments> excelHelper() {
        return Stream.of(
         Arguments.of(ExcelHelper.create(Product.class)),
         Arguments.of(ExcelHelper.create(Product.class, AnnotationType.CONSTRUCTOR))
        );
    }
    @ParameterizedTest
    @MethodSource("excelHelper")
    @Order(0)
    void toExcel_withValidProductData_shouldReturnCorrectExcel(ExcelHelper<Product> excelHelper) {
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("Product A", 100, 90.5, 80.0, true, false, 50, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.A, LocalDateTime.now()));
        productList.add(new Product("Product B", 200, 180.75, 150.0, true, true, 30, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B, LocalDateTime.now()));

        // Generate Excel file
        String destinationPath = "src/test/resources/products.xlsx";
        try (ByteArrayInputStream excelBytes = excelHelper.toExcel(productList);
                Workbook workbook = new XSSFWorkbook(excelBytes);
             FileOutputStream outputStream = new FileOutputStream(destinationPath);
        ) {
            workbook.write(outputStream);
            Sheet sheet = workbook.getSheetAt(0);
                assertEquals(2, sheet.getLastRowNum());
                // Verify headers
                Row headerRow = sheet.getRow( 0);
                assertEquals("name", headerRow.getCell(0).getStringCellValue());
                assertEquals("price", headerRow.getCell(1).getStringCellValue());
                assertEquals("promo price", headerRow.getCell(2).getStringCellValue());
                assertEquals("min price", headerRow.getCell(3).getStringCellValue());
                assertEquals("active", headerRow.getCell(4).getStringCellValue());
                assertEquals("expired", headerRow.getCell(5).getStringCellValue());
                assertEquals("units in stock", headerRow.getCell(6).getStringCellValue());
                assertEquals("created date", headerRow.getCell(7).getStringCellValue());
                assertEquals("updated date", headerRow.getCell(8).getStringCellValue());
                assertEquals("zoned date time", headerRow.getCell(9).getStringCellValue());
                assertEquals("category", headerRow.getCell(10).getStringCellValue());
                // Verify data rows
                for (int i = 0; i < productList.size(); i++) {
                    Row row = sheet.getRow(i + 1);
                    Product product = productList.get(i);

                    assertEquals(product.getName(), row.getCell(0).getStringCellValue());
                    assertEquals(product.getPrice(), row.getCell(1).getNumericCellValue());
                    assertEquals(product.getPromoPrice(), row.getCell(2).getNumericCellValue());
                    assertEquals(product.getMinPrice(), row.getCell(3).getNumericCellValue());
                    assertEquals(product.isActive(), row.getCell(4).getBooleanCellValue());
                    assertEquals(product.getExpired(), row.getCell(5).getBooleanCellValue());
                    assertEquals((double) product.getUnitsInStock(), row.getCell(6).getNumericCellValue());
                    assertEquals(new SimpleDateFormat().format(product.getCreatedDate()), row.getCell(7).getStringCellValue());
                    assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(product.getUpdatedDate()), row.getCell(8).getStringCellValue());
                    assertEquals(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(product.getZonedDateTime()), row.getCell(9).getStringCellValue());
                    assertEquals(product.getCategory().toString(), row.getCell(10).getStringCellValue());
                }
                } catch (IOException e) {
                fail("Error reading Excel file: " + e.getMessage());
            }
        }
    @ParameterizedTest
    @ValueSource(strings = {"products.xlsx","products2.xlsx","products3.xlsx"})
    @Order(1)
    void toList_withValidExcelFile_shouldReturnCorrectProductList(String fileName) throws IOException {

        // Read the file as an InputStream
       InputStream inputStream = new ClassPathResource(fileName).getInputStream();

        // Create a MockMultipartFile
        MultipartFile multipartFile = new MockMultipartFile("file", "generatedExcel.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", inputStream);
        List<Product> result = excelHelper.toList(multipartFile);

        // Assuming you know the expected size of the list
        assertEquals(2, result.size());

        // Add more assertions based on the actual data you expect
        // For example:
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
        assertNotNull(result.get(1).getUpdatedDate()); // Assuming it's not null in the Excel file
        assertNotNull(result.get(1).getZonedDateTime()); // Assuming it's not null in the Excel file
        assertNotNull(result.get(1).getCategory()); // Assuming it's not null in the Excel file
    }

    @Test
    @Order(2)
    void toList_withInvalidExcelFile_shouldThrowException() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "invalid.xlsx",
                getClass().getClassLoader().getResourceAsStream("invalid.xlsx")
        );

        assertThrows(ExcelFileException.class, () -> excelHelper.toList(file));
    }

    }
