[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=adnanebk_excel-pojo-converter)](https://sonarcloud.io/summary/new_code?id=adnanebk_excel-pojo-converter)

Converting Excel or CSV files into Java objects (POJOs) and vice versa can be a complex process, this is why i have created a java library that handles both excel and csv conversions to pojo objects in both directions (read and wrire ) by leveraguling Java reflection.

## Required dependencies
to use this library you have to add these dependencies, or only one of them and remove the folder that is responsible for other conversion type (csvpojoconverter or excelpojoconverter)

```
		<dependency>
			<groupId>org.apache.poi</groupId>
			<artifactId>poi-ooxml</artifactId>
			<version>5.3.0</version>
		</dependency>

		<dependency>
			<groupId>com.opencsv</groupId>
			<artifactId>opencsv</artifactId>
			<version>5.9</version>
		</dependency>
```

## Understanding the POJO Class

Before we dive into the library, let’s take a close look at a sample Java class that serves as our data model:

```
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @CellDefinition(0,converter=NameConverter.class)
    private String name;

    @CellDefinition(1)
    private long price;

    @CellDefinition(2)
    private boolean active;

    @CellDefinition(value = 3, title = "Promo price")
    private double promoPrice;

    // Additional fields...

    @CellDefinition(10,enumConverter =  CategoryConverter.class)
    private Category category;

    @CellDefinition(11)
    private LocalDateTime localDateTime;

}

public class CategoryConverter implements EnumConverter<Category> {

    
    @Override
    public Map<Category, String> convert() {
        return Map.of(Category.A,"aa", Category.B,"bb",Category.C,"cc");
    }
}
class NameConverter implements Converter<String> {

    @Override
    public String convertToCellValue(String fieldValue) {
        return fieldValue + " added text";
    }

    @Override
    public String convertToFieldValue(String cellValue) {
        return cellValue + " modified text";
    }
}
```
This Product class is annotated with various annotations that play a crucial role in the conversion process. Each field is annotated with @CellDefinition, indicating its cell position in the Excel or CSV file.

we can also define the title of the cell, by default it will convert the camel case name of the field to a name with spaces (ex: firstName=>First name)

we used the enumConverter attribute to define a converter class that contains the conversions (by using a map) between the enum constants and the cell values in the excel/csv (by default the enum constants will be used as cell values).


We can also use a custom converter to convert a field value to its cell value or the reverse, to do that we can define the
argument 'converter' to do both conversion sides or the arguments 'cellConverter', 'fieldConverter' to do one way of the conversion

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

we have just seen one way to do the convesions by using annotations, we can also use the programatic approch to do the same

```
                ExcelHelper.create(Product.class,
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

 
```

the same applicable for converting csv files except we need to define the delimiter that will be used

    private final CsvHelper<ProductV2> csvHelper = CsvHelper.create(ProductV2.class,";");
## Optimization considaration

One noteworthy feature of this library is the optimization applied to enhance performance. During initialization, constructor and all getters and setters are initialized only once and used subsequently. This  minimizes the need for reflection lookups in subsequent operations and boosts overall efficiency.

