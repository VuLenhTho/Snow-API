package com.vulenhtho.snow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private Integer year;

    private Integer month;

    private Long importMoney;

    private Long interestMoney;

    private Long moneyFromSale;

    private List<ProductInfoToReportDTO> bestsellerProduct = new ArrayList<>();

    private List<ProductInfoToReportDTO> badSellerProduct = new ArrayList<>();

    private List<ProductDTO> totalProduct = new ArrayList<>();

    private Map<Integer, Long> interestPerMonth = new HashMap<>();

    private Map<Integer, Long> importPerMonth = new HashMap<>();

    private Map<Integer, Long> moneyFromSalePerMonth = new HashMap<>();

    public ReportDTO(Integer year, Integer month, Long importMoney, Long interestMoney, Long moneyFromSale) {
        this.year = year;
        this.month = month;
        this.importMoney = importMoney;
        this.interestMoney = interestMoney;
        this.moneyFromSale = moneyFromSale;
    }

    public ReportDTO(Long importMoney, Long interestMoney, Long moneyFromSale, List<ProductDTO> totalProduct) {
        this.importMoney = importMoney;
        this.interestMoney = interestMoney;
        this.moneyFromSale = moneyFromSale;
        this.totalProduct = totalProduct;
    }
}
