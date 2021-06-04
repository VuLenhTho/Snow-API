package com.vulenhtho.snow.repository;

import com.vulenhtho.snow.entity.Color;
import com.vulenhtho.snow.entity.Product;
import com.vulenhtho.snow.entity.ProductColorSize;
import com.vulenhtho.snow.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ProductColorSizeRepository extends JpaRepository<ProductColorSize, Long> {
    @Transactional
    @Modifying
    @Query("delete from ProductColorSize p where p.product.id = ?1")
    void deleteByProductId(Long productId);

    ProductColorSize findByColorAndSizeAndProduct(Color color, Size size, Product product);


}
