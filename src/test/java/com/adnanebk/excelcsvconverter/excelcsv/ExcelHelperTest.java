package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.ToCellConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.heplers.ColumnDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.heplers.ExcelHelper;
import com.adnanebk.excelcsvconverter.excelcsv.models.BooleanConverter;
import com.adnanebk.excelcsvconverter.excelcsv.models.Category;
import com.adnanebk.excelcsvconverter.excelcsv.models.Product;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
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

import static com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter.DEFAULT_DATE_PATTERN;
import static com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter.DEFAULT_DATE_TIME_PATTERN;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExcelHelperTest {
    private final static ExcelHelper<Product> excelHelper = ExcelHelper.create(Product.class, createColumnsDefinitions());
    private final static ExcelHelper<Product> excelHelper2 = ExcelHelper.create(Product.class);

    private static ColumnDefinition[] createColumnsDefinitions() {
       return new ColumnDefinition[]{
               new ColumnDefinition(0, "name", "Name"),
               new ColumnDefinition(1, "price", "Price",(String e)->Long.parseLong(e)),
               new ColumnDefinition(2, "promoPrice", "Promotion price"),
               new ColumnDefinition(5, "expired", "Expired",new BooleanConverter()),
               new ColumnDefinition(3, "minPrice", "Min price"),
               new ColumnDefinition(4, "active", "Active"),
               new ColumnDefinition(6, "unitsInStock", "Units in stock"),
               new ColumnDefinition(7, "createdDate", "Created date"),
               new ColumnDefinition(8, "updatedDate", "Updated date"),
               new ColumnDefinition(9, "zonedDateTime", "Zoned date time"),
               new ColumnDefinition(10, "category", "Category", ()->Map.of(Category.A,"aa", Category.B,"bb", Category.C,"cc"),Category.class),
               new ColumnDefinition(11, "localDateTime", "Local date time")
       };
    }

    private static ToCellConverter<String> getStringToCellConverter() {
        return (v) -> v;
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
    void toExcel_withValidProductData_shouldReturnCorrectExcel(ExcelHelper<Product> excelHelper) {
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
                assertEquals(new SimpleDateFormat(DEFAULT_DATE_TIME_PATTERN).format(product.getCreatedDate()), row.getCell(7).getStringCellValue());
                assertEquals(DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN).format(product.getUpdatedDate()), row.getCell(8).getStringCellValue());
                assertEquals(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN).format(product.getZonedDateTime()), row.getCell(9).getStringCellValue());
                assertEquals("bb", row.getCell(10).getStringCellValue());
                assertEquals(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN).format(product.getLocalDateTime()), row.getCell(11).getStringCellValue());
        } catch (IOException e) {
            fail("Error reading Excel file: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("getAllHelpers")
    @Order(2)
    void toList_withValidExcelFile_shouldReturnCorrectProductList(ExcelHelper<Product> excelHelper) throws IOException {

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
            assertTrue(LocalDate.now().isEqual(result.get(1).getUpdatedDate())); // Assuming it's not null in the Excel file
            assertNotNull(result.get(1).getZonedDateTime()); // Assuming it's not null in the Excel file
            assertSame(Category.B, result.get(1).getCategory()); // Assuming it's not null in the Excel file
        }
    }
public static Stream<ExcelHelper<Product>> getAllHelpers(){
        return Stream.of(excelHelper,excelHelper2);
}
}
