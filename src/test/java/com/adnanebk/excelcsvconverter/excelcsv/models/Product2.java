package com.adnanebk.excelcsvconverter.excelcsv.models;

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
@SheetDefinition(includeAllFields = true,datePattern = "yyyy-dd-MM",dateTimePattern = "yyyy-dd-MM HH:mm")
public class Product2 {


    private String name;


    private long price;

    private double promoPrice;

    private Double minPrice;

    @IgnoreCell
    private boolean active;

    private Boolean expired;


    private Integer unitsInStock;


    private Date createdDate;


    private LocalDate updatedDate;


    private ZonedDateTime zonedDateTime;

    private Category category;


    private LocalDateTime localDateTime;


}
