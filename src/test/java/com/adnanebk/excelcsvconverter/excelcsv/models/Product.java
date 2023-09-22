package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellEnumValues;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.SheetDefinition;
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
@SheetDefinition
public class Product {

    @CellDefinition(0)
    private String name;

    @CellDefinition(1)
    private long price;
    @CellDefinition(value = 2,title = "Promo price")
    private double promoPrice;

    @CellDefinition(3)
    private Double minPrice;

    @CellDefinition(4)
    private boolean active;

    @CellDefinition(5)
    private Boolean expired;


    @CellDefinition(6)
    private Integer unitsInStock;

    @CellDefinition(7)
    private Date createdDate;

    @CellDefinition(8)
    private LocalDate updatedDate;

    @CellDefinition(9)
    private ZonedDateTime zonedDateTime;

    @CellEnumValues(value = {"aa","bb","cc"})
    @CellDefinition(10)
    private Category category;

    @CellDefinition(11)
    private LocalDateTime localDateTime;

}
