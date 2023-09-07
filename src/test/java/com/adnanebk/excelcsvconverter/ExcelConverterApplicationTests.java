package com.adnanebk.excelcsvconverter;

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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
class ExcelConverterApplicationTests {

    private ExcelHelper<Product> excelHelper;

    @BeforeEach
    void setUp() {
        excelHelper = ExcelHelper.create(Product.class);
    }
    @Test
    @Order(0)
    void toExcel_withValidProductData_shouldReturnCorrectExcel() throws IOException {
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("Product A", 100, 90.5, 80.0, true, false, 50, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.A));
        productList.add(new Product("Product B", 200, 180.75, 150.0, true, true, 30, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B));

        // Generate Excel file
        String destinationPath = "src/test/resources/products.xlsx";
        try (ByteArrayInputStream excelBytes = excelHelper.toExcel(productList);
                Workbook workbook = new XSSFWorkbook(excelBytes);
             FileOutputStream outputStream = new FileOutputStream(destinationPath)
        ) {
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
                    assertEquals(product.getPrice(), row.getCell(1).getNumericCellValue(), 0.001);
                    assertEquals(product.getPromoPrice(), row.getCell(2).getNumericCellValue(), 0.001);
                    assertEquals(product.getMinPrice(), row.getCell(3).getNumericCellValue(), 0.001);
                    assertEquals(product.isActive(), row.getCell(4).getBooleanCellValue());
                    assertEquals(product.getExpired(), row.getCell(5).getBooleanCellValue());
                    assertEquals((double) product.getUnitsInStock(), row.getCell(6).getNumericCellValue(), 0.001);
                    // Assuming dates are written in the correct format, you can use SimpleDateFormat for detailed comparison
                    assertEquals(product.getCreatedDate().toString(), row.getCell(7).getDateCellValue().toString());
                    assertEquals(product.getUpdatedDate().toString(), row.getCell(8).getLocalDateTimeCellValue().toLocalDate().toString());
                    // For ZonedDateTime, you may need to parse the string and compare
                    assertTrue(product.getZonedDateTime().toLocalDateTime().toString().contains(row.getCell(9).getLocalDateTimeCellValue().atZone(ZoneId.systemDefault()).toLocalDateTime().toString()));
                    assertEquals(product.getCategory().toString(), row.getCell(10).getStringCellValue());
                }
                workbook.write(outputStream);
                } catch (IOException e) {
                fail("Error reading Excel file: " + e.getMessage());
            }
        }
    @Test
    @Order(1)
    void toList_withValidExcelFile_shouldReturnCorrectProductList() throws IOException {

        // Read the file as an InputStream
       InputStream inputStream = new ClassPathResource("products.xlsx").getInputStream();

        // Create a MockMultipartFile
        MultipartFile multipartFile = new MockMultipartFile("file", "generatedExcel.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", inputStream);
        List<Product> result = excelHelper.toList(multipartFile);

        // Assuming you know the expected size of the list
        assertEquals(2, result.size());

        // Add more assertions based on the actual data you expect
        // For example:
        assertEquals("Product A", result.get(0).getName());
        assertEquals(100, result.get(0).getPrice());
        assertEquals(90.5, result.get(0).getPromoPrice(), 0.001); // Delta for double comparison
        assertTrue(result.get(0).isActive());
        assertFalse(result.get(0).getExpired());
        assertEquals(50, result.get(0).getUnitsInStock());
        assertEquals("Product B", result.get(1).getName());
        assertEquals(200, result.get(1).getPrice());
        assertEquals(180.75, result.get(1).getPromoPrice(), 0.001); // Delta for double comparison
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
