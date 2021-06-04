package com.vulenhtho.snow.repository;

import com.vulenhtho.snow.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SizeRepository extends JpaRepository<Size,Long> {
    Size findByName(String name);

}
