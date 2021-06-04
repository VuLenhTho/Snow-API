package com.vulenhtho.snow.service;

import com.vulenhtho.snow.dto.ProductColorSizeDTO;
import com.vulenhtho.snow.dto.ProductDTO;
import com.vulenhtho.snow.dto.ProductDetailDTO;
import com.vulenhtho.snow.dto.request.ProductFilterRequestDTO;
import com.vulenhtho.snow.dto.response.*;
import com.vulenhtho.snow.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    Product create(ProductDTO productDTO);

    void update(ProductDetailDTO productDetailDTO);

    void addProductColorSize(ProductColorSizeDTO productColorSizeDTO);

    ProductWebResponseDTO findForWebById(Long id);

    ListProductPageResponse getWindowViewByFilterForWeb(ProductFilterRequestDTO filterRequest);

    Page<ProductWebResponseDTO> getAllWithFilterForWeb(ProductFilterRequestDTO filterRequest);

    ProductDTO findById(Long id);

    ProductDetailDTO getProductDetailByAdmin(Long id);

    InfoToCreateProductDTO getInfoToCreateProductDTO();

    Page<ProductDTO> getAllWihFilter(ProductFilterRequestDTO filterRequest);

    boolean delete(Long id);

    boolean delete(List<Long> ids);

    WebHomeResponse getDataForWebHomePage();

    PageHeaderDTO getHeaderResponse();


}
