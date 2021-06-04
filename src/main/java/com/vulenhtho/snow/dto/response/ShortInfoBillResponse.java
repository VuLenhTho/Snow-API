package com.vulenhtho.snow.dto.response;

import com.vulenhtho.snow.entity.enumeration.BillStatus;
import com.vulenhtho.snow.entity.enumeration.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ShortInfoBillResponse {

    private Long id;

    private String receiver;

    private String phone;

    private Instant createdDate;

    private Long finalPayMoney;

    private PaymentMethod paymentMethod;

    private BillStatus status;
}
