package com.example.cartreactivedemo.dto;

public enum ReturnCode {
    OK("200");

    ReturnCode(String code) {
        this.code = code;
    }

    String code;

    public String getCode() {
        return this.code;
    }
}
