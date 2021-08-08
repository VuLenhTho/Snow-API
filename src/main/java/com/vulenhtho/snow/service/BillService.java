package com.vulenhtho.snow.service;

import com.vulenhtho.snow.dto.BillDTO;
import com.vulenhtho.snow.dto.ItemDTO;
import com.vulenhtho.snow.dto.ReportDTO;
import com.vulenhtho.snow.dto.UpdateBillDTO;
import com.vulenhtho.snow.dto.request.AddAnItemIntoBillDTO;
import com.vulenhtho.snow.dto.request.BillFilterRequest;
import com.vulenhtho.snow.dto.request.CartDTO;
import com.vulenhtho.snow.dto.response.ItemsForCartAndHeader;
import com.vulenhtho.snow.dto.response.ShortInfoBillResponse;
import com.vulenhtho.snow.security.exception.ObjectIsNullException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BillService {

    ItemsForCartAndHeader getItemShowInCart(List<ItemDTO> itemDTOS);

    void createBill(CartDTO cartDTO);

    Page<ShortInfoBillResponse> getListShortInfoBill(BillFilterRequest filterRequest) throws ObjectIsNullException;

    void delete(List<Long> billIds);

    void delete(Long id);

    BillDTO getBillDetail(Long id);

    void updateBillByAdmin(UpdateBillDTO updateBillDTO);

    void addItemIntoBill(AddAnItemIntoBillDTO addAnItemIntoBillDTO);

    ReportDTO getReportByMonthAndYear(Integer month, Integer year);

    ReportDTO getReportByYear(Integer year);

    ReportDTO getReportByRangeDate(Long startTime, Long endTime);

}
