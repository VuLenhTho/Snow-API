package com.vulenhtho.snow.mapper;

import com.vulenhtho.snow.dto.SizeDTO;
import com.vulenhtho.snow.entity.Size;
import com.vulenhtho.snow.repository.SizeRepository;
import com.vulenhtho.snow.util.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SizeMapper {

    private final SizeRepository sizeRepository;

    public SizeMapper(SizeRepository sizeRepository) {
        this.sizeRepository = sizeRepository;
    }

    public SizeDTO toDTO(Size size){
        SizeDTO sizeDTO = new SizeDTO();
        BeanUtils.refine(size, sizeDTO, BeanUtils::copyNonNull);
        return sizeDTO;
    }

    public Set<SizeDTO> toDTO(Set<Size> sizes){
        return sizes.stream().map(this::toDTO).collect(Collectors.toSet());
    }

    public Size toEntity(SizeDTO sizeDTO){
        return sizeRepository.getOne(sizeDTO.getId());
    }

    public Set<Size> toEntity(Set<SizeDTO> sizeDTOS){
        return sizeDTOS.stream().map(this::toEntity).collect(Collectors.toSet());
    }
}
