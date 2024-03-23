package com.sunshine.qiaoke.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunshine.qiaoke.Dao.PurchaseRecord;

import java.util.Date;
import java.util.List;

public interface PurchaseRecordService extends IService<PurchaseRecord> {

    // 购买记录列表查询
    List<PurchaseRecord> listPurchaseRecord(String name , Date startTime, Date endTime, int calFlag);

    // 需要参与结算收益的购买记录
    List<PurchaseRecord> getRecordToCal(Long buyerId);

    // 查询单个人员
    PurchaseRecord getPurchaseRecord(Long id);

    // 新增购买记录
    Integer addPurchaseRecord(PurchaseRecord record);

    Integer updatePurchaseRecord(PurchaseRecord record);

    Integer delPurchaseRecord(Long id);

}
