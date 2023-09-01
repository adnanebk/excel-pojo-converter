package com.example.excelConverter.controllers;

import com.example.excelConverter.excel.ExcelHelper;
import com.example.excelConverter.excel.exceptions.ExcelFileException;
import com.example.excelConverter.excel.exceptions.ExcelValidationException;
import com.example.excelConverter.excel.exceptions.ReflectionException;
import com.example.excelConverter.excel.models.AnnotationType;
import com.example.excelConverter.models.Category;
import com.example.excelConverter.models.Product;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("products")
public class ExcelFieldsController {
    private final ExcelHelper<Product> excelHelper = ExcelHelper.create(Product.class);

    @GetMapping
    public List<Product> excelToProducts(@RequestParam MultipartFile file){
        var s = System.currentTimeMillis();
      var r =  excelHelper.excelToList(file);
        var e = System.currentTimeMillis();
        System.out.println(e-s);
        return r;

    }
    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource>
    downloadExcelFromProducts() {
         String filename = "products-" + LocalDate.now() + ".xlsx";
        InputStreamResource file = new InputStreamResource(excelHelper.listToExcel(getProducts()));
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
                .minPrice(50.4).build();
    }

    @ExceptionHandler(value = { ReflectionException.class, ExcelValidationException.class, ExcelFileException.class })
    protected ResponseEntity<String> handleExceptions(
            RuntimeException ex) {
           return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
