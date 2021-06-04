package com.vulenhtho.snow.dto.response;

import com.vulenhtho.snow.dto.DiscountDTO;
import com.vulenhtho.snow.dto.SubCategoryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfoToCreateProductDTO {

    private Set<DiscountDTO> discountDTOS = new HashSet<>();

    private Set<SubCategoryDTO> subCategoryDTOS = new HashSet<>();
}
