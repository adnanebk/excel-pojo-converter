package com.adnanebk.excelcsvconverter.controllers;


import com.adnanebk.excelcsvconverter.excelcsv.ExcelHelper;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelFileException;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;
import com.adnanebk.excelcsvconverter.models.Category;
import com.adnanebk.excelcsvconverter.models.Product;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("products")
public class ExcelFieldsController {
    private final ExcelHelper<Product> excelHelper = ExcelHelper.create(Product.class);

    @GetMapping
    public List<Product> excelToProducts(@RequestBody MultipartFile file){
        return excelHelper.toList(file);
    }
    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource>
    downloadExcelFromProducts() {
        String filename = "products-" + LocalDate.now() + ".xlsx";
        InputStreamResource file = new InputStreamResource(excelHelper.toExcel(getProducts()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }


    public List<Product> getProducts(){
        return IntStream.range(0,20).mapToObj(e->getProduct()).toList();
    }
    public Product getProduct(){
        return  Product.builder()
                .expired(true)
                .active(false)
                .category(Category.B)
                .unitsInStock(10)
                .updatedDate(LocalDate.now())
                .price(200)
                .name("P1")
                .createdDate(new Date())
                .zonedDateTime(ZonedDateTime.now())
                .promoPrice(90.5)
                .localDateTime(LocalDateTime.now())
                .minPrice(50.4).build();
    }

    @ExceptionHandler(value = { ReflectionException.class, ExcelValidationException.class, ExcelFileException.class })
    protected ResponseEntity<String> handleExceptions(
            RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
