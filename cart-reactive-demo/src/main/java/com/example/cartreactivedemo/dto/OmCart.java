package com.example.cartreactivedemo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.swing.*;
import java.time.LocalDateTime;

@Data
@Table("om_cart")
@NoArgsConstructor
@AllArgsConstructor
public class OmCart {

    @Id
    private String cartSn;
    private String mbNo;
    private String trNo;
    private String lrtrNo;
    private String spdNo;
    private String sitmNo;
    private Integer odQty;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime regDttm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime modDttm;

    public String getId() {
        return this.cartSn;
    }
}
