package com.example.cartreactivedemo.dto.api;

import com.example.cartreactivedemo.dto.OmCart;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReturnDto {
    private String returnCode;
    private String message;
    private List<OmCart> data;
}
