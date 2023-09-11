package com.adnanebk.excelcsvconverter.models;

import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellEnumValues;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.IgnoreCell;
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
@SheetDefinition(includeAllFields = true)
public class ProductAllFields {

    private String name;

    private long price;

    private double promoPrice;

    private Double minPrice;

    private boolean active;

    private Boolean expired;


    private Integer unitsInStock;

    private Date createdDate;

    private LocalDate updatedDate;

    private ZonedDateTime zonedDateTime;

    @CellEnumValues(values = {"aa","bb","cc"})
    private Category category;

    @IgnoreCell
    private LocalDateTime localDateTime;

}
