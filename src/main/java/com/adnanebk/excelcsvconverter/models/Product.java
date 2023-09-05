package com.adnanebk.excelcsvconverter.models;

import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.ConstructorCells;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    public Product(String name, long price, double promoPrice, Double minPrice, boolean active, Boolean expired, Date createdDate) {
        this.name = name;
        this.price = price;
        this.promoPrice = promoPrice;
        this.minPrice = minPrice;
        this.active = active;
        this.expired = expired;
        this.createdDate = createdDate;
    }
}
