package com.sunshine.qiaoke.vo;

import lombok.Data;

import java.util.Date;

@Data
public class PurchaseRecordQuery {

    private int pageNum;

    private int pageSize;

    private String name;

    private Date startTime;

    private Date endTime;

    private int calFlag;

}
