package com.vulenhtho.snow.mapper;

import com.vulenhtho.snow.dto.ItemDTO;
import com.vulenhtho.snow.entity.Item;
import com.vulenhtho.snow.util.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ItemDTO toDTO(Item item) {
        ItemDTO itemDTO = new ItemDTO();
        BeanUtils.refine(item, itemDTO, BeanUtils::copyNonNull);
        itemDTO.setProductId(item.getProduct().getId());
        itemDTO.setProductName(item.getProduct().getName());
        itemDTO.setThumbnail(item.getProduct().getThumbnail());
        itemDTO.setImportPrice(item.getImportPrice());
        return itemDTO;
    }
}
