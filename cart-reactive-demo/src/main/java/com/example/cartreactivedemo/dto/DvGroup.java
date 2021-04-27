package com.example.cartreactivedemo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DvGroup {

    public String trNo;
    public String trNm;
    public boolean checked;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    public LocalDateTime regDttm;

    public List<OmCart> omCartList;

}
