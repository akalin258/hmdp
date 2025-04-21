package com.hmdp.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// SeckillMessage.java
@Data
@NoArgsConstructor
public class SeckillMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private Long voucherId;

    private Long userId;

    public SeckillMessage(Long voucherId,Long userId,Long orderId){
        this.voucherId=voucherId;
        this.userId=userId;
        this.orderId=orderId;
    }

}
