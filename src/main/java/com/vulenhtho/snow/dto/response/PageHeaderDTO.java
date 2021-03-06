package com.vulenhtho.snow.dto.response;

import com.vulenhtho.snow.dto.CategoryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageHeaderDTO {

    private List<CategoryDTO> categoryDTOS = new ArrayList<>();

    private List<String> discounts = new ArrayList<>();
}
