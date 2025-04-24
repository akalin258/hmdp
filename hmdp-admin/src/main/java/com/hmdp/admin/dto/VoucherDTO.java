package com.hmdp.admin.dto;



import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoucherDTO {
    // Voucher基本信息
    private Long id;
    private Long shopId;
    private String title;
    private String subTitle;
    private String rules;
    private Long payValue;
    private Long actualValue;
    private Integer type;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // SeckillVoucher特有信息
    private Integer stock;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;

    // 额外信息(可选)
    private String shopName; // 店铺名称，用于显示
}