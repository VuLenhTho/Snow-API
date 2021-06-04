package com.vulenhtho.snow.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemShowInCartDTO {

    private Long id;

    private String name;

    private String color;

    private String size;

    private String thumbnail;

    private Long price;

    private Long importPrice;

    private Long quantity;

    private Long totalPrice;
}
