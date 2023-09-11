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
    @CellEnumValues(values = {"aa","bb","cc"})
    private Category category;

    @CellDefinition
    private LocalDateTime localDateTime;

}
