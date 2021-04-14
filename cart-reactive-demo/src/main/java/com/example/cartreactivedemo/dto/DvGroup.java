package com.example.cartreactivedemo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DvGroup {

    public String trNo;
    public String trNm;
    public LocalDateTime regDttm;

    public List<OmCart> omCartList;

}
