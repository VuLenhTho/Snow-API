package com.vulenhtho.snow.mapper;

import com.vulenhtho.snow.dto.BillDTO;
import com.vulenhtho.snow.dto.ItemDTO;
import com.vulenhtho.snow.dto.response.ShortInfoBillResponse;
import com.vulenhtho.snow.entity.Bill;
import com.vulenhtho.snow.security.exception.ObjectIsNullException;
import com.vulenhtho.snow.util.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BillMapper {

    private final ItemMapper itemMapper;

    public BillMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public ShortInfoBillResponse toShortInfoBillResponse(Bill bill) {
        ShortInfoBillResponse shortInfoBillResponse = new ShortInfoBillResponse();
        BeanUtils.refine(bill, shortInfoBillResponse, BeanUtils::copyNonNull);
        return shortInfoBillResponse;
    }

    public List<ShortInfoBillResponse> toShortInfoBillResponse(List<Bill> bills) throws ObjectIsNullException {
        if (CollectionUtils.isEmpty(bills)) {
            throw new ObjectIsNullException("List Bll is empty");
        }
        return bills.stream().map(this::toShortInfoBillResponse).collect(Collectors.toList());
    }

    public BillDTO toDTO(Bill bill) {
        BillDTO result = new BillDTO();
        BeanUtils.refine(bill, result, BeanUtils::copyNonNull);
        result.setUserId(bill.getUser().getId());
        Set<ItemDTO> itemDTOS = bill.getItems().stream().map(itemMapper::toDTO).collect(Collectors.toSet());
        result.setItemDTOS(itemDTOS);
        return result;
    }
}
