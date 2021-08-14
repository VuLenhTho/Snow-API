package com.vulenhtho.snow.service.impl;

import com.vulenhtho.snow.dto.*;
import com.vulenhtho.snow.dto.request.AddAnItemIntoBillDTO;
import com.vulenhtho.snow.dto.request.BillFilterRequest;
import com.vulenhtho.snow.dto.request.CartDTO;
import com.vulenhtho.snow.dto.response.ItemShowInCartDTO;
import com.vulenhtho.snow.dto.response.ItemsForCartAndHeader;
import com.vulenhtho.snow.dto.response.ShortInfoBillResponse;
import com.vulenhtho.snow.entity.*;
import com.vulenhtho.snow.entity.enumeration.BillStatus;
import com.vulenhtho.snow.mapper.BillMapper;
import com.vulenhtho.snow.mapper.DiscountMapper;
import com.vulenhtho.snow.mapper.ProductMapper;
import com.vulenhtho.snow.repository.*;
import com.vulenhtho.snow.service.BillService;
import com.vulenhtho.snow.service.ProductService;
import com.vulenhtho.snow.specification.BillSpecification;
import com.vulenhtho.snow.util.BeanUtils;
import com.vulenhtho.snow.util.CommonUtils;
import com.vulenhtho.snow.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillServiceImpl implements BillService {

    private final ProductRepository productRepository;

    private final DiscountMapper discountMapper;

    private final BillRepository billRepository;

    private final ItemRepository itemRepository;

    private final ProductMapper productMapper;

    private final ColorRepository colorRepository;

    private final DiscountRepository discountRepository;

    private final SizeRepository sizeRepository;

    private final ProductService productService;

    private final BillMapper billMapper;

    private final SecurityUtils securityUtils;

    private final ProductColorSizeRepository productColorSizeRepository;

    public BillServiceImpl(ProductRepository productRepository, DiscountMapper discountMapper, BillRepository billRepository, ItemRepository itemRepository, ProductMapper productMapper
            , ColorRepository colorRepository, DiscountRepository discountRepository, SizeRepository sizeRepository, ProductService productService, BillMapper billMapper, SecurityUtils securityUtils
            , ProductColorSizeRepository productColorSizeRepository) {
        this.productRepository = productRepository;
        this.discountMapper = discountMapper;
        this.billRepository = billRepository;
        this.itemRepository = itemRepository;
        this.productMapper = productMapper;
        this.colorRepository = colorRepository;
        this.discountRepository = discountRepository;
        this.sizeRepository = sizeRepository;
        this.productService = productService;
        this.billMapper = billMapper;
        this.securityUtils = securityUtils;
        this.productColorSizeRepository = productColorSizeRepository;
    }

    @Override
    public ItemsForCartAndHeader getItemShowInCart(List<ItemDTO> itemDTOS) {
        List<ItemShowInCartDTO> itemShowInCartDTOS = itemDTOS.stream().map(itemDTO -> {
            Product product = productRepository.getOne(itemDTO.getProductId());
            return productMapper.toItemShowInCartDTO(product, itemDTO);
        }).collect(Collectors.toList());
        Set<DiscountDTO> discountDTOS = discountMapper.toDTO(discountRepository.getByInTimeDiscountAndForBill(Instant.now()));
        return new ItemsForCartAndHeader(itemShowInCartDTOS, discountDTOS, productService.getHeaderResponse());
    }

    @Override
    public void createBill(CartDTO cartDTO) {
        Bill billToSave = new Bill();

        BeanUtils.refine(cartDTO, billToSave, BeanUtils::copyNonNull);
        if (cartDTO.getAccountNumber() != null && cartDTO.getAccountName() != null) {
            billToSave.setPaymentInfo(cartDTO.getAccountName() + "," + cartDTO.getAccountNumber() + "," + cartDTO.getTradingCode());
        }
        billToSave.setStatus(BillStatus.INIT);
        billToSave.setUser(securityUtils.getCurrentUserLogin());

        Bill finalBillToSave = billRepository.save(billToSave);
        Set<Item> items = cartDTO.getItemList().stream().map(itemDTO -> {
            Item item = new Item();
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(itemDTO.getPrice());
            Product product = productRepository.getOne(itemDTO.getProductId());
            Color color = colorRepository.getOne(itemDTO.getColorId());
            Size size = sizeRepository.getOne(itemDTO.getSizeId());
            item.setColor(color.getName());
            item.setSize(size.getName());
            item.setProduct(product);
            item.setImportPrice(product.getImportPrice());
            item.setBill(finalBillToSave);
            return item;
        }).collect(Collectors.toSet());

        itemRepository.saveAll(items);
    }

    @Override
    public Page<ShortInfoBillResponse> getListShortInfoBill(BillFilterRequest filterRequest) {
        return billRepository.findAll(BillSpecification.filterBill(filterRequest)
                , PageRequest.of(filterRequest.getPage()
                        , filterRequest.getSize()
                        , CommonUtils.getSort(filterRequest.getSort()))
        ).map(billMapper::toShortInfoBillResponse);
    }

    @Override
    public void delete(Long id) {
        Optional<Bill> bill = billRepository.findById(id);
        if (bill.isPresent()) {
            if (!BillStatus.INIT.equals(bill.get().getStatus()) && !BillStatus.CHECKING.equals(bill.get().getStatus())) {
                handleChangeStatusToBeforeCONFIRMED(bill.get().getItems());
            }
            itemRepository.deleteAll(bill.get().getItems());
            billRepository.delete(bill.get());
        }
    }

    @Override
    public void delete(List<Long> ids) {
        ids.forEach(this::delete);
    }

    @Override
    public BillDTO getBillDetail(Long id) {
        Optional<Bill> bill = billRepository.findById(id);
        return bill.map(billMapper::toDTO).orElse(null);
    }

    @Override
    @Transactional
    public void updateBillByAdmin(UpdateBillDTO updateBillDTO) {
        Bill currentBill = billRepository.findById(updateBillDTO.getBillDTO().getId()).get();
        BillStatus oldStatus = currentBill.getStatus(); //status before set new data to bill
        setNewBillDataOnUpdate(currentBill, updateBillDTO);

        if (checkChangeBillStatusToCONFIRMED(oldStatus, currentBill.getStatus())) {
            handleChangeStatusToCONFIRMED(currentBill.getItems());
        }
        if (checkChangeBillStatusToBeforeCONFIRMED(oldStatus, currentBill.getStatus())) {
            handleChangeStatusToBeforeCONFIRMED(currentBill.getItems());
        }
        billRepository.save(currentBill);
    }

    private boolean checkChangeBillStatusToCONFIRMED(BillStatus oldStatus, BillStatus newStatus) {
        return (BillStatus.INIT.equals(oldStatus) || BillStatus.CHECKING.equals(oldStatus)) && (!BillStatus.INIT.equals(newStatus) && !BillStatus.CHECKING.equals(newStatus));
    }

    private boolean checkChangeBillStatusToBeforeCONFIRMED(BillStatus oldStatus, BillStatus newStatus) {
        return (!BillStatus.INIT.equals(oldStatus) && !BillStatus.CHECKING.equals(oldStatus)) && (BillStatus.INIT.equals(newStatus) || BillStatus.CHECKING.equals(newStatus));
    }

    private void setNewBillDataOnUpdate(Bill billToUpdate, UpdateBillDTO updateBillDTO) {
        BillDTO newBill = updateBillDTO.getBillDTO();
        BillStatus oldStatus = billToUpdate.getStatus();

        billToUpdate.setAddress(newBill.getAddress());
        billToUpdate.setStatus(newBill.getStatus());
        billToUpdate.setReceiver(newBill.getReceiver());
        billToUpdate.setPhone(newBill.getPhone());
        billToUpdate.setPaymentMethod(newBill.getPaymentMethod());
        billToUpdate.setPaymentInfo(newBill.getPaymentInfo());
        billToUpdate.setNote(newBill.getNote());

        //Only change the quantity before CONFIRMED status
        boolean isBeforeCONFIRMEDStatus = BillStatus.INIT.equals(oldStatus) || BillStatus.CHECKING.equals(oldStatus);
        if (updateBillDTO.getProductIdsToDel() != null) {
            List<Long> productIdsToDel = Arrays.stream(updateBillDTO.getProductIdsToDel().split(","))
                    .filter(st -> !StringUtils.isEmpty(st))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<Item> itemsToRemove = new ArrayList<>();
            for (Long idToDel : productIdsToDel) {
                for (Item item : billToUpdate.getItems()) {
                    if (item.getProduct().getId().equals(idToDel)) {
                        if (!isBeforeCONFIRMEDStatus) {
                            handleChangeStatusToBeforeCONFIRMED(new HashSet<>(Collections.singleton(item)));
                        }
                        itemsToRemove.add(item);
                    }
                }
            }
            billToUpdate.getItems().removeAll(itemsToRemove);
            itemRepository.deleteAll(itemsToRemove);
        }

        List<Long> quantityOfProducts = Arrays.stream(updateBillDTO.getQuantityOfProducts().split(","))
                .filter(st -> !StringUtils.isEmpty(st))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        List<Long> productIds = Arrays.stream(updateBillDTO.getProductIds().split(","))
                .filter(st -> !StringUtils.isEmpty(st))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        billToUpdate.getItems().forEach(item -> {
            for (int i = 0; i < productIds.size(); i++) {
                if (isSameProductAndCanChangeQuantity(item, productIds.get(i), quantityOfProducts.get(i))) {
                    if (isBeforeCONFIRMEDStatus) {
                        item.setQuantity(quantityOfProducts.get(i));
                    } else {
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "CanNotChangeQuantityAfterCONFIRMEDStatus");
                    }
                }
            }
        });
        Long newFinalPayMoney = billToUpdate.getItems().stream().mapToLong(item -> item.getQuantity() * item.getPrice()).sum();
        Long newTotalImportPrice = billToUpdate.getItems().stream().mapToLong(item -> item.getQuantity() * item.getImportPrice()).sum();

        billToUpdate.setFinalPayMoney(newFinalPayMoney);
        billToUpdate.setTotalImportMoney(newTotalImportPrice);
    }

    private void handleChangeStatusToCONFIRMED(Set<Item> items) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        for (Item item : items) {
            ProductColorSize productColorSize = productColorSizeRepository.findByColorAndSizeAndProduct(
                    colorRepository.findByName(item.getColor()), sizeRepository.findByName(item.getSize()), item.getProduct());
            Long newQuantity = productColorSize.getQuantity() - item.getQuantity();
            productColorSize.setQuantity(newQuantity);

            productColorSizeRepository.save(productColorSize);
        }
    }

    private void handleChangeStatusToBeforeCONFIRMED(Set<Item> items) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        for (Item item : items) {
            ProductColorSize productColorSize = productColorSizeRepository.findByColorAndSizeAndProduct(
                    colorRepository.findByName(item.getColor()), sizeRepository.findByName(item.getSize()), item.getProduct());
            Long newQuantity = productColorSize.getQuantity() + item.getQuantity();
            productColorSize.setQuantity(newQuantity);

            productColorSizeRepository.save(productColorSize);
        }
    }

    private boolean isSameProductAndCanChangeQuantity(Item item, Long compareId, Long quantity) {
        boolean isSameProduct = item.getProduct().getId().equals(compareId);
        if (!isSameProduct || quantity.equals(item.getQuantity())) {
            return false;
        }
        Product product = productRepository.findById(item.getProduct().getId()).get();
        boolean isNotChangePrice = item.getPrice().equals(product.getPrice());
        if (!isNotChangePrice) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "PriceHasChanged");
        }
        return true;
    }

    @Override
    public void addItemIntoBill(AddAnItemIntoBillDTO addAnItemIntoBillDTO) {
        Product product = productRepository.findByName(addAnItemIntoBillDTO.getProductInfo());
        Color color = colorRepository.findByName(addAnItemIntoBillDTO.getColorInfo());
        Size size = sizeRepository.findByName(addAnItemIntoBillDTO.getSizeInfo());
        try {
            if (product == null) {
                product = productRepository.findById(Long.parseLong(addAnItemIntoBillDTO.getProductInfo())).get();
            }
            if (color == null) {
                color = colorRepository.findById(Long.parseLong(addAnItemIntoBillDTO.getColorInfo())).get();
            }
            if (size == null) {
                size = sizeRepository.findById(Long.parseLong(addAnItemIntoBillDTO.getSizeInfo())).get();
            }
        } catch (Exception e) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "NotFound");
        }
        ProductColorSize productColorSize = productColorSizeRepository.findByColorAndSizeAndProduct(color, size, product);
        if (productColorSize.getQuantity() < addAnItemIntoBillDTO.getQuantity()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "NotEnoughProductQuantity");
        }
        Bill bill = billRepository.findById(addAnItemIntoBillDTO.getBillId()).get();
        Long price = productMapper.countPriceInDiscount(discountMapper.toDTO(product.getDiscounts()), product.getPrice());
        Item item = new Item(null, color.getName(), size.getName()
                , price, product.getImportPrice(), addAnItemIntoBillDTO.getQuantity(), bill, product);
        itemRepository.save(item);

        boolean isBeforeCONFIRMEDStatus = BillStatus.INIT.equals(bill.getStatus()) || BillStatus.CHECKING.equals(bill.getStatus());
        if (!isBeforeCONFIRMEDStatus) {
            handleChangeStatusToCONFIRMED(new HashSet<>(Collections.singleton(item)));
        }
    }

    @Override
    public ReportDTO getReportByMonthAndYear(Integer month, Integer year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Instant fistDayOfMonth = calendar.toInstant();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
        Instant lastDayOfMonth = calendar.toInstant();

        List<Bill> bills = billRepository.getByLastModifiedDateAndStatus(fistDayOfMonth, lastDayOfMonth, BillStatus.FINISH);

        return getReportByBillList(bills, month, year);
    }


    private void getProductInfoByBill(Bill bill, List<ProductInfoToReportDTO> productHasSale) {
        bill.getItems().forEach(item -> {
            Optional<ProductInfoToReportDTO> hasExisting = productHasSale.stream().filter(product -> product.getId().equals(item.getProduct().getId())).findFirst();
            if (hasExisting.isPresent()) {
                int index = productHasSale.indexOf(hasExisting.get());
                hasExisting.get().setQuantity(hasExisting.get().getQuantity() + item.getQuantity());
                productHasSale.set(index, hasExisting.get());
            } else {
                Product product = item.getProduct();
                ProductInfoToReportDTO newProductInfo = new ProductInfoToReportDTO(product.getId(), product.getName(), product.getThumbnail(), item.getQuantity());
                productHasSale.add(newProductInfo);
            }
        });

    }


    @Override
    public ReportDTO getReportByYear(Integer year) {
        Calendar firstDayOfYear = Calendar.getInstance();
        firstDayOfYear.set(Calendar.YEAR, year);
        firstDayOfYear.set(Calendar.DAY_OF_YEAR, 1);

        Calendar lastDayOfYear = Calendar.getInstance();
        lastDayOfYear.set(Calendar.YEAR, year);
        lastDayOfYear.set(Calendar.MONTH, 11); // 11 = december
        lastDayOfYear.set(Calendar.DAY_OF_MONTH, 31);

        List<Bill> bills = billRepository.getByLastModifiedDateAndStatus(firstDayOfYear.toInstant(), lastDayOfYear.toInstant(), BillStatus.FINISH);

        List<ProductDTO> totalProduct = new ArrayList<>();
        for (Bill bill : bills) {
            for (Item item : bill.getItems()) {
                totalProduct.add(productMapper.toDTO(item.getProduct()));
            }
        }
        ReportDTO reportDTO = getReportByBillList(bills, null, year);
        reportDTO.setTotalProduct(totalProduct);

        return reportDTO;
    }

    @Override
    public ReportDTO getReportByRangeDate(Long startTime, Long endTime) {
        List<Bill> bills = billRepository.getByLastModifiedDateAndStatus(Instant.ofEpochMilli(startTime), Instant.ofEpochMilli(endTime), BillStatus.FINISH);

        Long totalImportMoney = bills.stream().mapToLong(Bill::getTotalImportMoney).sum();
        Long totalMoneyFromSale = bills.stream().mapToLong(Bill::getFinalPayMoney).sum();
        Long interestMoney = totalMoneyFromSale - totalImportMoney;

        List<ProductDTO> totalProduct = new ArrayList<>();
        for (Bill bill : bills) {
            for (Item item : bill.getItems()) {
                totalProduct.add(productMapper.toDTO(item.getProduct()));
            }
        }
        ReportDTO result = new ReportDTO(totalImportMoney, interestMoney, totalMoneyFromSale, totalProduct);

        return result;
    }


    private ReportDTO getReportByBillList(List<Bill> bills, Integer month, Integer year) {
        Long totalImportMoney = bills.stream().mapToLong(Bill::getTotalImportMoney).sum();
        Long totalMoneyFromSale = bills.stream().mapToLong(Bill::getFinalPayMoney).sum();
        Long interestMoney = totalMoneyFromSale - totalImportMoney;

        List<ProductInfoToReportDTO> productHasSale = new ArrayList<>();
        for (Bill bill : bills) {
            getProductInfoByBill(bill, productHasSale);
        }

        ReportDTO result = new ReportDTO(year, month, totalImportMoney, interestMoney, totalMoneyFromSale);
        if (productHasSale.size() < 1) {
            return result;
        }
        productHasSale.sort((o1, o2) -> o2.getQuantity().compareTo(o1.getQuantity()));

        for (ProductInfoToReportDTO productInfoToReportDTO : productHasSale) {
            if (result.getBestsellerProduct().size() < 10) {
                result.getBestsellerProduct().add(productInfoToReportDTO);
            }
        }

        for (int i = productHasSale.size(); i > 0; i--) {
            if (result.getBadSellerProduct().size() < 10) {
                result.getBadSellerProduct().add(productHasSale.get(i - 1));
            }
        }

         Map<Integer, Long> interestPerMonth = initMapReportPerMonth();
         Map<Integer, Long> importPerMonth = initMapReportPerMonth();
         Map<Integer, Long> moneyFromSalePerMonth = initMapReportPerMonth();

        for (Bill bill : bills) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(bill.getLastModifiedDate(), ZoneId.systemDefault());
            int monthCreate = localDateTime.getMonthValue();
            importPerMonth.put(monthCreate, importPerMonth.get(monthCreate) + bill.getTotalImportMoney());
            moneyFromSalePerMonth.put(monthCreate, moneyFromSalePerMonth.get(monthCreate) + bill.getFinalPayMoney());

            Long interest = bill.getFinalPayMoney() - bill.getTotalImportMoney();
            interestPerMonth.put(monthCreate, interestPerMonth.get(monthCreate) + interest);
        }
        result.setInterestPerMonth(interestPerMonth);
        result.setImportPerMonth(importPerMonth);
        result.setMoneyFromSalePerMonth(moneyFromSalePerMonth);

        return result;
    }

    private Map<Integer, Long> initMapReportPerMonth(){
        Map<Integer, Long> map = new HashMap<>();
        for (int i = 1; i < 13; i++) {
            map.put(i, 0L);
        }
        return map;
    }
}
