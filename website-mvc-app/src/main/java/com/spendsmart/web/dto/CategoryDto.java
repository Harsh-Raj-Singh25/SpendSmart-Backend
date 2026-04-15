package com.spendsmart.web.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryDto {
    private Integer categoryId;
    private String name;
    private String type;
    private String icon;
}

