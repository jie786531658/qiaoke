package com.sunshine.qiaoke.vo;

import lombok.Data;

import java.util.List;

@Data
public class BuyerVo {

    private Long id;

    private String name;

    private String text;

    private Long introducerId;

    private int buyCount;

    private int allCount;

    private List<BuyerVo> nodes;

}
