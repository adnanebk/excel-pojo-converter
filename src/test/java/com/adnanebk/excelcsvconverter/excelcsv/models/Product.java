package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellBoolean;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @CellDefinition(0)
    private String name;

    @CellDefinition(1)
    private long price;
    @CellDefinition(value = 2,title = "Promo price")
    private double promoPrice;

    @CellDefinition(5)
    @CellBoolean(trueValue = "yes",falseValue = "no")
    private Boolean expired;

    @CellDefinition(3)
    private Double minPrice;

    @CellDefinition(4)
    private boolean active;
    @CellDefinition(6)
    private Integer unitsInStock;

    @CellDefinition(7)
    private Date createdDate;

    @CellDefinition(8)
    private LocalDate updatedDate;

    @CellDefinition(9)
    private ZonedDateTime zonedDateTime;

    @CellEnum(enumsMapperMethod = "categoryMap")
    @CellDefinition(10)
    private Category category;

    @CellDefinition(11)
    private LocalDateTime localDateTime;

    private Map<Category,String> categoryMap(){
        return Map.of(Category.A,"aa",
                Category.B,"bb",
                Category.C,"cc"
        );
    }

    public Product(String name, long price, double promoPrice, Double minPrice, boolean active, Boolean expired, Integer unitsInStock, Date createdDate, LocalDate updatedDate, ZonedDateTime zonedDateTime, Category category, LocalDateTime localDateTime) {
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
        this.localDateTime = localDateTime;
    }

}
