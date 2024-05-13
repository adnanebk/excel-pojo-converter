[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=adnanebk_excel-pojo-converter)](https://sonarcloud.io/summary/new_code?id=adnanebk_excel-pojo-converter)

Converting Excel or CSV files into Java objects (POJOs) and vice versa can be a complex process, this is why i have created a java library that handles both excel and csv conversions to pojo objects in both directions (read and wrire ) by leveraguling Java reflection.

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

    @CellDefinition(0,converter=NameConverter.class)
    private String name;

    @CellDefinition(1)
    private long price;

    @CellDefinition(2)
    @CellBoolean(trueValue = "yes",falseValue = "no")
    private boolean active;

    @CellDefinition(value = 3, title = "Promo price")
    private double promoPrice;

    // Additional fields...

    @CellEnum(converter = CategoryConverter.class)
    @CellDefinition(10)
    private Category category;

    @CellDefinition(11)
    private LocalDateTime localDateTime;

}

public class CategoryConverter implements EnumConverter<Category> {
    Map<Category,String> map =  Map.of(Category.A,"aa",
            Category.B,"bb",
            Category.C,"cc"
    );
    
    @Override
    public Map<Category, String> convert() {
        return map;
    }
}
class NameConverter implements Converter<String> {

    @Override
    public String convertToCellValue(String fieldValue) {
        return fieldValue + " added text";
    }

    @Override
    public String convertToFieldValue(String cellValue) {
        return cellValue + " added";
    }
}
```
This Product class is annotated with various annotations that play a crucial role in the conversion process. Each field is annotated with @CellDefinition, indicating its position in the Excel or CSV file.

we can also define the title of the cell, by default it will convert the camel case name of the field to a name with spaces (ex: firstName=>First name)

The @SheetDefinition annotation provides additional information like date formatting patterns that will be used during conversion of date field types.

we make use of the @CellEnum annotation in the enum Category field, and we define a converter class that contains the conversions (by using a map) between the enum constants and the cell values in the excel/csv (by default the enum constants will be used as cell values).

we make use of the @CellBoolean annotation in the boolean active field,it has two arguments witch represents the converted cells values in the excel/csv file .

We can also use a custom converter to convert a field value to its cell value, to do that we define
the converter argument in the cellDefinition annotation like the example in the name field, or we can use the annotation @CellConverter(converter = MyConverter.class)  

Now, let’s introduce a second version of our POJO class, ProductV2:
```
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SheetDefinition(includeAllFields = true,titles={"Name","Category","Date"})
public class ProductV2 {

    private String name;

    // Additional fields...

    private Category category;

    @IgnoreCell
    private LocalDateTime localDateTime;
}
```

when includeAllFields argument set to true the fields are automatically included and mapped in the cells based on its declared order and ignoring fields that annotated with @IgnoreCell annotation,

we can define the titles in the titles argument with the role that they must be in the same order as the fields.

## Converting Excel/csv to POJOs and vice versa
```
@RestController
@RequestMapping("excel/products")
public class ExcelFieldsController {
    private final ExcelHelper<Product> excelHelper = ExcelHelper.create(Product.class);

    @GetMapping
    public List<Product> excelToProducts(@RequestBody MultipartFile file){
        return excelHelper.toStream(file.getInputStream()).toList();
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
## Optimization considaration

One noteworthy feature of this library is the optimization applied to enhance performance. During initialization, constructor and all getters and setters are initialized only once and used subsequently. This  minimizes the need for reflection lookups in subsequent operations and boosts overall efficiency.

