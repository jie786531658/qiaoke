package com.sunshine.qiaoke.vo;

import lombok.Data;

import java.util.List;

@Data
public class Buyer2Vo {
    private String text;
    private List<Buyer2Vo> nodes;
}
