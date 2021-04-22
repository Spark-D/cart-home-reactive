package com.example.cartreactivedemo.dto;

import com.example.cartreactivedemo.dto.api.ProductRes;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Table("om_cart")
@NoArgsConstructor
@AllArgsConstructor
public class OmCart {

    @Id
    private String cartSn;
    @CreatedBy
    private String mbNo;
    private String trNo;
    private String lrtrNo;
    private String spdNo;
    private String sitmNo;
    private Integer odQty;

    @Transient
    private String lrtrNm;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime regDttm;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime modDttm;

    public String getId() {
        return this.cartSn;
    }

    @Transient
    @With
    private Object product;

    @Override
    @Transient
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public OmCart(String cartSn, String trNo, String lrtrNo, String sitmNo, int odQty, LocalDateTime regDttm) {
        this.cartSn = cartSn;
        this.trNo = trNo;
        this.lrtrNo = lrtrNo;
        this.sitmNo = sitmNo;
        this.odQty = odQty;
        this.regDttm = regDttm;
    }

}
