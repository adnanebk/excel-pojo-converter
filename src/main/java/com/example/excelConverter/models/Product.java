package com.example.excelConverter.models;

import com.example.excelConverter.excel.annotations.ExcelCol;
import com.example.excelConverter.excel.annotations.ExcelColsDefinition;
import com.example.excelConverter.excel.annotations.ExcelConstructorParameters;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@ExcelColsDefinition(dateFormat = "dd/MM/yyyy",dateTimeFormat = "dd MMM yyyy HH:mm")
public class Product {


    @ExcelCol(index = 0)
    private String name;

    @ExcelCol
    private long price;
    @ExcelCol
    private double promoPrice;

    @ExcelCol
    private Double minPrice;

    @ExcelCol
    private boolean active;

    @ExcelCol
    private Boolean expired;


    @ExcelCol(index = 1)
    private Integer unitsInStock;

    @ExcelCol
    private Date createdDate;

    @ExcelCol
    private LocalDate updatedDate;

    @ExcelCol
    private ZonedDateTime zonedDateTime;

    @ExcelCol(index = 3)
    private Category category;


    @ExcelConstructorParameters
    public Product(String name, long price, double promoPrice, Double minPrice, boolean active, Boolean expired, Integer unitsInStock, Date createdDate, LocalDate updatedDate, ZonedDateTime zonedDateTime,Category category) {
        this.name = name;
        this.price = price;
        this.promoPrice = promoPrice;
        this.minPrice = minPrice;
        this.active = active;
        this.expired = expired;
        this.unitsInStock = unitsInStock;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.zonedDateTime = zonedDateTime;
        this.category=category;
    }
}
