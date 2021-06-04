package com.vulenhtho.snow.entity;

import com.vulenhtho.snow.entity.enumeration.BillStatus;
import com.vulenhtho.snow.entity.enumeration.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bill")
@Getter
@Setter
public class Bill extends AbstractAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String receiver;

    @Column
    private String phone;

    @Column
    private String address;

    @Column
    private Long shippingCosts;

    @Column
    private Long finalPayMoney;

    @Column
    private Long totalImportMoney;

    @Column
    private Long totalMoney;

    @Column
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column
    private String paymentInfo;

    @Column
    private String note;

    @Column
    @Enumerated(EnumType.STRING)
    private BillStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL)
    private Set<Item> items = new HashSet<>();

}
