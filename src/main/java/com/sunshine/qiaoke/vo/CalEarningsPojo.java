package com.sunshine.qiaoke.vo;

import lombok.Data;

/*
计算收益时，使用的临时对象
 */
@Data
public class CalEarningsPojo {

    //人员ID
    private Long id;

    //总计购买份额
    private Integer ultiCount;

    //收益
    private Integer earnings;

    //计算公式
    private StringBuilder str;

}
