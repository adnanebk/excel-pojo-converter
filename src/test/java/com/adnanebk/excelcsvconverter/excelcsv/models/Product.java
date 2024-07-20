package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @CellDefinition(value = 0)
    private String name;

    @CellDefinition(1)
    private long price;
    @CellDefinition(value = 2,title = "Promotion price")
    private double promoPrice;

    @CellDefinition(value = 5,converter = BooleanConverter.class)
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

    @CellDefinition(value = 10,enumConverter = CategoryConverter.class)
    private Category category;

    @CellDefinition(11)
    private LocalDateTime localDateTime;

    private String otherProp;

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
