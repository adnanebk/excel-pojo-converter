package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellEnumFormat;
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
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SheetDefinition(includeAllFields = true,datePattern = "dd/MM/yyyy")
public class Product2 {


    private String name;


    private long price;

    private double promoPrice;

    private Double minPrice;

    @IgnoreCell
    private boolean active;
    @IgnoreCell
    private Boolean expired;


    private Integer unitsInStock;


    private Date createdDate;


    private LocalDate updatedDate;


    private ZonedDateTime zonedDateTime;

    @CellEnumFormat(enumsMapperMethod = "categoryMap")
    @CellDefinition(10)
    private Category category;


    private LocalDateTime localDateTime;

    private Map<Category,String> categoryMap(){
        return Map.of(Category.A,"aa",
                Category.B,"bb",
                Category.C,"cc"
        );
    }


}
