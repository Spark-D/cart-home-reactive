package com.example.cartreactivedemo.dto.api;

import lombok.Data;

import java.util.List;

@Data
public class CatePdList {

    public Integer totalCount;
    public Integer pageNo;
    public Integer rowsPerPage;
    public Integer startIndex;
    public List<DataList> dataList;

}
