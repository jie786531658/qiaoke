package com.sunshine.qiaoke.vo;

import lombok.Data;

import java.util.Date;

@Data
public class EarningsQuery {

    private int pageNum;

    private int pageSize;

    private String name;

    private Date startTime;

    private Date endTime;

    // 结算状态（0，未结算；1，已结算）
    private int calFlag = 1;

}