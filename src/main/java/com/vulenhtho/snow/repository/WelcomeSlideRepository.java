package com.vulenhtho.snow.repository;

import com.vulenhtho.snow.entity.WelcomeSlide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WelcomeSlideRepository extends JpaRepository<WelcomeSlide, Long> {
    List<WelcomeSlide> getByIsDisabled(Boolean isDisabled);
}
