package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.models.Category;
import com.adnanebk.excelcsvconverter.excelcsv.models.Product;
import com.adnanebk.excelcsvconverter.excelcsv.models.Product2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExcelHelperTest {
    private final ExcelHelper<Product> excelHelper = ExcelHelper.create(Product.class);
    private final ExcelHelper<Product2> excelHelper2 = ExcelHelper.create(Product2.class);

    @BeforeEach
    void setUp() {
    }

    private static List<Product> getProducts() {
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("Product A", 100, 90.5, 80.0, true, false, 50, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B, LocalDateTime.now()));
        productList.add(new Product("Product B", 200, 180.75, 150.0, true, true, 30, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B,LocalDateTime.now()));
        return productList;
    }
    private static List<Product2> getProducts2() {
        List<Product2> productList = new ArrayList<>();
        productList.add(new Product2("Product A", 100, 90.5, 80.0, true, false, 50, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B, LocalDateTime.now()));
        productList.add(new Product2("Product B", 200, 180.75, 150.0, true, true, 30, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B,LocalDateTime.now()));
        return productList;
    }
    @Test
    @Order(0)
    void toExcel_includeAllOption_withValidProductData_shouldReturnCorrectExcel() {
        List<Product2> productList = getProducts2();

        // Generate Excel file
        String destinationPath = "src/test/resources/products2.xlsx";
        try (ByteArrayInputStream excelBytes = excelHelper2.toExcel(productList);
             Workbook workbook = new XSSFWorkbook(excelBytes);
             FileOutputStream outputStream = new FileOutputStream(destinationPath)
        ) {
            workbook.write(outputStream);
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(2, sheet.getLastRowNum());
            // Verify headers
            Row headerRow = sheet.getRow( 0);
            assertEquals("Name", headerRow.getCell(0).getStringCellValue());
            assertEquals("Price", headerRow.getCell(1).getStringCellValue());
            assertEquals("Promo price", headerRow.getCell(2).getStringCellValue());
            assertEquals("Min price", headerRow.getCell(3).getStringCellValue());
            assertEquals("Units in stock", headerRow.getCell(4).getStringCellValue());
            assertEquals("Created date", headerRow.getCell(5).getStringCellValue());
            assertEquals("Updated date", headerRow.getCell(6).getStringCellValue());
            assertEquals("Zoned date time", headerRow.getCell(7).getStringCellValue());
            assertEquals("Category", headerRow.getCell(8).getStringCellValue());
            // Verify data rows
            for (int i = 0; i < productList.size(); i++) {
                Row row = sheet.getRow(i + 1);
                Product2 product = productList.get(i);

                assertEquals(product.getName(), row.getCell(0).getStringCellValue());
                assertEquals(product.getPrice(), row.getCell(1).getNumericCellValue());
                assertEquals(product.getPromoPrice(), row.getCell(2).getNumericCellValue());
                assertEquals(product.getMinPrice(), row.getCell(3).getNumericCellValue());
                assertEquals((double) product.getUnitsInStock(), row.getCell(4).getNumericCellValue());
                assertEquals(new SimpleDateFormat().format(product.getCreatedDate()), row.getCell(5).getStringCellValue());
                assertEquals(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(product.getUpdatedDate()), row.getCell(6).getStringCellValue());
                assertEquals(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(product.getZonedDateTime()), row.getCell(7).getStringCellValue());
                assertEquals("bb", row.getCell(8).getStringCellValue());
                assertTrue(product.getLocalDateTime().toString().startsWith(row.getCell(9).getStringCellValue()));
            }
        } catch (IOException e) {
            fail("Error reading Excel file: " + e.getMessage());
        }
    }
    @Test
    @Order(1)
    void toExcel_withValidProductData_shouldReturnCorrectExcel() {
        List<Product> productList = getProducts();

        // Generate Excel file
        String destinationPath = "src/test/resources/products.xlsx";
        try (ByteArrayInputStream excelBytes = excelHelper.toExcel(productList);
                Workbook workbook = new XSSFWorkbook(excelBytes);
             FileOutputStream outputStream = new FileOutputStream(destinationPath)
        ) {
            workbook.write(outputStream);
            Sheet sheet = workbook.getSheetAt(0);
                assertEquals(2, sheet.getLastRowNum());
                // Verify headers
                Row headerRow = sheet.getRow( 0);
                assertEquals("Name", headerRow.getCell(0).getStringCellValue());
                assertEquals("Price", headerRow.getCell(1).getStringCellValue());
                assertEquals("Promo price", headerRow.getCell(2).getStringCellValue());
                assertEquals("Min price", headerRow.getCell(3).getStringCellValue());
                assertEquals("Active", headerRow.getCell(4).getStringCellValue());
                assertEquals("Expired", headerRow.getCell(5).getStringCellValue());
                assertEquals("Units in stock", headerRow.getCell(6).getStringCellValue());
                assertEquals("Created date", headerRow.getCell(7).getStringCellValue());
                assertEquals("Updated date", headerRow.getCell(8).getStringCellValue());
                assertEquals("Zoned date time", headerRow.getCell(9).getStringCellValue());
                assertEquals("Category", headerRow.getCell(10).getStringCellValue());
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
                    assertEquals(product.getUpdatedDate().toString(), row.getCell(8).getStringCellValue());
                    assertEquals(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(product.getZonedDateTime()), row.getCell(9).getStringCellValue());
                    assertEquals("bb", row.getCell(10).getStringCellValue());
                    assertTrue(product.getLocalDateTime().toString().startsWith(row.getCell(11).getStringCellValue()));
                }
                } catch (IOException e) {
                fail("Error reading Excel file: " + e.getMessage());
            }
        }

    @Test
    @Order(2)
    void toList_withValidExcelFile_shouldReturnCorrectProductList() throws IOException {

        // Read the file as an InputStream
        String destinationPath = "src/test/resources/products.xlsx";
        // Read the file as an InputStream
        try (InputStream inputStream = Files.newInputStream(new File(destinationPath).toPath())) {

            List<Product> result = excelHelper.toStream(inputStream).toList();
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
            assertTrue(LocalDate.now().isEqual(result.get(1).getUpdatedDate())); // Assuming it's not null in the Excel file
            assertNotNull(result.get(1).getZonedDateTime()); // Assuming it's not null in the Excel file
            assertSame(Category.B, result.get(1).getCategory()); // Assuming it's not null in the Excel file
        }
    }
    @Test
    @Order(3)
    void toList_withValidExcelFile_includeAllOption_shouldReturnCorrectProductList() throws IOException {

        // Read the file as an InputStream
        String destinationPath = "src/test/resources/products.xlsx";
        // Read the file as an InputStream
        try (InputStream inputStream = Files.newInputStream(new File(destinationPath).toPath())) {
            // Create a MockMultipartFile
            // MultipartFile multipartFile = new MockMultipartFile("file", "generatedExcel.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", inputStream);
            List<Product2> result = excelHelper2.toStream(inputStream).toList();

            // Assuming you know the expected size of the list
            assertEquals(2, result.size());

            // Add more assertions based on the actual data you expect
            // For example:

            assertEquals("Product B", result.get(1).getName());
            assertEquals(200, result.get(1).getPrice());
            assertEquals(180.75, result.get(1).getPromoPrice()); // Delta for double comparison
            assertFalse(result.get(1).isActive());
            assertNull(result.get(1).getExpired());
            assertEquals(30, result.get(1).getUnitsInStock());
            assertNotNull(result.get(1).getCreatedDate()); // Assuming it's not null in the Excel file
            assertTrue(LocalDate.now().isEqual(result.get(1).getUpdatedDate())); // Assuming it's not null in the Excel file
            assertNotNull(result.get(1).getZonedDateTime()); // Assuming it's not null in the Excel file
            assertSame(Category.B, result.get(1).getCategory()); // Assuming it's not null in the Excel file
        }
    }

    }
