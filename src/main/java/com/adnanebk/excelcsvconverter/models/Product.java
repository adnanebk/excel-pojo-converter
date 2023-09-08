package com.adnanebk.excelcsvconverter.models;

import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.ConstructorCells;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.SheetDefinition;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@SheetDefinition(dateFormat = "dd MMM yyyy")
public class Product {
    @CellDefinition
    private String name;

    @CellDefinition
    private long price;
    @CellDefinition
    private double promoPrice;

    @CellDefinition
    private Double minPrice;

    @CellDefinition
    private boolean active;

    @CellDefinition
    private Boolean expired;


    @CellDefinition
    private Integer unitsInStock;

    @CellDefinition
    private Date createdDate;

    @CellDefinition
    private LocalDate updatedDate;

    @CellDefinition
    private ZonedDateTime zonedDateTime;

    @CellDefinition
    private Category category;

    @ConstructorCells
    public Product(String name, long price, double promoPrice, Double minPrice, boolean active, Boolean expired, Integer unitsInStock, Date createdDate, LocalDate updatedDate, ZonedDateTime zonedDateTime, Category category) {
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
        this.category = category;
    }
}
