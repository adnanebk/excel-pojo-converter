package com.adnanebk.excelcsvconverter.controllers;


import com.adnanebk.excelcsvconverter.excelcsv.ExcelHelper;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelFileException;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;
import com.adnanebk.excelcsvconverter.models.Category;
import com.adnanebk.excelcsvconverter.models.ProductV2;
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
import java.util.stream.Stream;

@RestController
@RequestMapping("excel/products/v2")
public class ExcelAllFieldsController {
    private final ExcelHelper<ProductV2> excelHelper = ExcelHelper.create(ProductV2.class);

    @GetMapping
    public Stream<ProductV2> excelToProducts(@RequestBody MultipartFile file){
        return   excelHelper.toStream(file);

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


    public List<ProductV2> getProducts(){
        return IntStream.range(0,20).mapToObj(e->getProduct()).toList();
    }
    public ProductV2 getProduct(){
        return  ProductV2.builder()
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
