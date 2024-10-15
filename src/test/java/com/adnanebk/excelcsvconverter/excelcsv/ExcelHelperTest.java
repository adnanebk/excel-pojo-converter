package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.core.ColumnDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.excelpojoconverter.ExcelHelper;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.SheetValidationException;
import com.adnanebk.excelcsvconverter.excelcsv.models.BooleanConverter;
import com.adnanebk.excelcsvconverter.excelcsv.models.Category;
import com.adnanebk.excelcsvconverter.excelcsv.models.Product;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExcelHelperTest {
    private  static ExcelHelper<Product> excelHelper;
    private  static ExcelHelper<Product> excelHelper2;

    @BeforeAll
    static void setUp() {
        excelHelper2 = ExcelHelper.create(Product.class);
        excelHelper = ExcelHelper.create(Product.class,
                ColumnDefinition.create(0, "name", "Name"),
                ColumnDefinition.createWithCellConverter(1, "price", "Price", Long::parseLong),
                ColumnDefinition.create(2, "promoPrice", "Promotion price"),
                ColumnDefinition.create(5, "expired", "Expired",new BooleanConverter()),
                ColumnDefinition.create(3, "minPrice", "Min price"),
                ColumnDefinition.create(4, "active", "Active"),
                ColumnDefinition.create(6, "unitsInStock", "Units in stock"),
                ColumnDefinition.create(7, "createdDate", "Created date"),
                ColumnDefinition.create(8, "updatedDate", "Updated date"),
                ColumnDefinition.create(9, "zonedDateTime", "Zoned date time"),
                ColumnDefinition.create(10, "category", "Category", ()->Map.of(Category.A,"aa", Category.B,"bb", Category.C,"cc"),Category.class),
                ColumnDefinition.create(11, "localDateTime", "Local date time")
        );
    }

    private static List<Product> getProducts() {
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("Product A", 100, 90.5, 80.0, true, true, 50, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B, LocalDateTime.now()));
        productList.add(new Product("Product B", 200, 180.75, 150.0, false, false, 30, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B,LocalDateTime.now()));
        return productList;
    }

    @ParameterizedTest
    @MethodSource("getAllHelpers")
    @Order(1)
    void toExcel_addColumnValidProductData_shouldReturnCorrectExcel(ExcelHelper<Product> excelHelper) {
        List<Product> productList = getProducts();

        // Generate Excel file
        String destinationPath = "src/test/resources/products3.xlsx";
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
            assertEquals("Promotion price", headerRow.getCell(2).getStringCellValue());
            assertEquals("Min price", headerRow.getCell(3).getStringCellValue());
            assertEquals("Active", headerRow.getCell(4).getStringCellValue());
            assertEquals("Expired", headerRow.getCell(5).getStringCellValue());
            assertEquals("Units in stock", headerRow.getCell(6).getStringCellValue());
            assertEquals("Created date", headerRow.getCell(7).getStringCellValue());
            assertEquals("Updated date", headerRow.getCell(8).getStringCellValue());
            assertEquals("Zoned date time", headerRow.getCell(9).getStringCellValue());
            assertEquals("Category", headerRow.getCell(10).getStringCellValue());
            // Verify data rows

                Row row = sheet.getRow(1);
                Product product = productList.get(0);

                assertEquals(product.getName(), row.getCell(0).getStringCellValue());
                assertEquals(product.getPrice(), row.getCell(1).getNumericCellValue());
                assertEquals(product.getPromoPrice(), row.getCell(2).getNumericCellValue());
                assertEquals(product.getMinPrice(), row.getCell(3).getNumericCellValue());
                assertEquals("true", row.getCell(4).getStringCellValue());
                assertEquals("Yes", row.getCell(5).getStringCellValue());
                assertEquals((double) product.getUnitsInStock(), row.getCell(6).getNumericCellValue());
                assertEquals(new SimpleDateFormat().format(product.getCreatedDate()), row.getCell(7).getStringCellValue());
                assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(product.getUpdatedDate()), row.getCell(8).getStringCellValue());
                assertEquals(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(product.getZonedDateTime()), row.getCell(9).getStringCellValue());
                assertEquals("bb", row.getCell(10).getStringCellValue());
                assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(product.getLocalDateTime()), row.getCell(11).getStringCellValue());
        } catch (IOException e) {
            fail("Error reading Excel file: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("getAllHelpers")
    @Order(2)
    void toList_addColumnValidExcelFile_shouldReturnCorrectProductList(ExcelHelper<Product> excelHelper) throws IOException {

        // Read the file as an InputStream
        String destinationPath = "src/test/resources/products3.xlsx";
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
            assertTrue(result.get(0).getExpired());
            assertEquals(50, result.get(0).getUnitsInStock());
            assertEquals("Product B", result.get(1).getName());
            assertEquals(200, result.get(1).getPrice());
            assertEquals(180.75, result.get(1).getPromoPrice()); // Delta for double comparison
            assertFalse(result.get(1).isActive());
            assertFalse(result.get(1).getExpired());
            assertEquals(30, result.get(1).getUnitsInStock());
            assertNotNull(result.get(1).getCreatedDate()); // Assuming it's not null in the Excel file
            assertEquals(LocalDate.now(),result.get(1).getUpdatedDate()); // Assuming it's not null in the Excel file
            assertNotNull(result.get(1).getZonedDateTime()); // Assuming it's not null in the Excel file
            assertSame(Category.B, result.get(1).getCategory()); // Assuming it's not null in the Excel file
        }
    }
    @Test
    void throw_field_not_found_exception(){
       Assertions.assertThrows(ReflectionException.class,()->ExcelHelper.create(Product.class, ColumnDefinition.create(0,"namee","Name"))
       );
    }
    @Test
    void throw_invalid_value_exception() throws IOException {
        String destinationPath = "src/test/resources/products3.xlsx";
        var helper = ExcelHelper.create(Product.class , ColumnDefinition.create(1,"name","Name"));
        try (InputStream inputStream = Files.newInputStream(new File(destinationPath).toPath())) {
            Assertions.assertThrows(SheetValidationException.class,()-> helper.toStream(inputStream).toList());
        }

    }
public static Stream<ExcelHelper<Product>> getAllHelpers(){
        return Stream.of(excelHelper,excelHelper2);
}
}
