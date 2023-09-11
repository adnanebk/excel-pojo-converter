package com.adnanebk.excelcsvconverter.models;

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
@SheetDefinition(dateFormat = "dd/MM/yyyy")
public class Product {

    private String name;
    private long price;
    private double promoPrice;

    @CellDefinition(3)
    private Double minPrice;

    private boolean active;

    @CellDefinition(4)
    private Boolean expired;


    @CellDefinition(5)
    private Integer unitsInStock;

    @CellDefinition(6)
    private Date createdDate;

    @CellDefinition(7)
    private LocalDate updatedDate;

    @CellDefinition(8)
    private ZonedDateTime zonedDateTime;

    @CellEnumValues(values = {"aa","bb","cc"})
    @CellDefinition(9)
    private Category category;

    private LocalDateTime localDateTime;

}
