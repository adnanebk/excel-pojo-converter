[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=adnanebk_excel-pojo-converter)](https://sonarcloud.io/summary/new_code?id=adnanebk_excel-pojo-converter)

Converting Excel or CSV files into Java objects (POJOs) and vice versa can be a complex process, but with the right tools and techniques, it becomes much more manageable. In this guide, we’ll explore a powerful Java library that leverages Java reflection.

## First we add the dependency to maven

```
<dependency>
  <groupId>com.adnanebk</groupId>
  <artifactId>excel-csv-converter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```
## Understanding the POJO Class

Before we dive into the library, let’s take a close look at a sample Java class that serves as our data model:

```
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SheetDefinition(datePattern = "dd/MM/yyyy")
public class Product {

    @CellDefinition(0)
    private String name;

    @CellDefinition(1)
    private long price;

    @CellDefinition(value = 2, title = "Promo price")
    private double promoPrice;

    // Additional fields...

   @CellEnumFormat(enumsMapperMethod = "categoryMap")
    @CellDefinition(10)
    private Category category;

    @CellDefinition(11)
    private LocalDateTime localDateTime;

    private Map<Category,String> categoryMap(){
        return Map.of(Category.A,"Formatted A",
                      Category.B,"Formatted B");
    }
}
```
This Product class is annotated with various annotations that play a crucial role in the conversion process. Each field is annotated with @CellDefinition, indicating its position in the Excel or CSV file.

we can also define the title of the of the cell, by default it will convert the camel case name of the field to a name with spaces (ex: firstName=>First name)

The @SheetDefinition annotation provides additional information like date formatting patterns that will be used during conversion of date field types.

### The Enum Annotation: @CellEnumFormat(enumsMapperMethod = "categoryMap")

In the Product class, we make use of the @CellEnumFormat annotation in the enum Category field. the enumsMapperMethod argument allows us to define a method name, this method should return a map that define the mapping (conversions) between the enum constants and the formatted values in the excel/csv cells (by default the enum constants will be used)
note that the method name must much the enumsMapperMethod argument value.

Now, let’s introduce an updated version of our POJO class, ProductV2:
```
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SheetDefinition(includeAllFields = true)
public class ProductV2 {

    private String name;

    // Additional fields...

    private Category category;

    @IgnoreCell
    private LocalDateTime localDateTime;
}
```
Converting Excel/csv to POJOs and vice versa
Converting Excel files to POJOs becomes even simpler with the annotation provided in the class to map the fields to corresponding cells in the Excel file.

With theProductV2 class, annotated with@SheetDefinition(includeAllFields = true) fields are automatically mapped in sequential order starting from index 0 and ignoring fields that have@IgnoreCell annotation.

```
@RestController
@RequestMapping("excel/products")
public class ExcelFieldsController {
    private final ExcelHelper<Product> excelHelper = ExcelHelper.create(Product.class);

    @GetMapping
    public List<Product> excelToProducts(@RequestBody MultipartFile file){
        return excelHelper.toStream(file).toList();
    }

       @GetMapping("/download")
    public ResponseEntity<InputStreamResource>
    downloadExcelFromProducts() {
        String filename = "products-" + LocalDate.now() + ".xlsx";
        InputStreamResource file = new InputStreamResource(excelHelper.toExcel(getProducts()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
```
the same applicable for converting csv files except we need to define the delimiter that will be used

    private final CsvHelper<ProductV2> csvHelper = CsvHelper.create(ProductV2.class,";");
## The ReflectionUtil Class: Dynamic Class Examination

The ReflectionUtil class serves as the backbone of this Java library, facilitating dynamic class examination and manipulation through the power of Java reflection. It plays a pivotal role in the seamless conversion of Excel and CSV files to Java objects (POJOs) and vice versa.

```public T createInstanceAndSetValues(Object[] values``` This method is a of the ReflectionUtil class. It create instance of a specified class T. Subsequently, it iterates through provided values and sets corresponding fields, thereby initializing the object for further processing.

One noteworthy feature of the ReflectionUtil class is the optimization applied to enhance performance. During initialization, all getters, setters, and fields are eagerly loaded and encapsulated in the custom field class. This deliberate action minimizes the need for reflection lookups in subsequent operations and boosts overall efficiency.

## Field Record Overview
```public record Field<T>(String name, Class<?> type, String title, Method getter, Method setter, int colIndex, Map<?,?> enumMapper)```

The Field record is a fundamental component of the library, designed to encapsulate information about a class field. It includes attributes such as field name, type, title, corresponding getter and setter methods, index for column mapping, and enumMapper that that maps enum values to constants and vise versa.

### Key Methods:
```public Object getValue(T obj)```: Retrieves the value of the field from an object using its getter method. If the field is an enum, it provides formatted values based on defined enum mappings.

```public void setValue(Object obj, Object value)```: Sets the value of the field in an object using its setter method. It handles enum values and ensures proper conversion.

## Conclusion :
By leveraging this custom library, developers can significantly simplify the process of converting Excel and CSV files to POJOs in Java. The integration of Java reflection, along with thoughtful design considerations, empowers dynamic mapping, making it a valuable tool for data processing tasks.
