package com.sunshine.qiaoke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunshine.qiaoke.Dao.PurchaseRecord;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PurchaseRecordMapper extends BaseMapper<PurchaseRecord> {

    // 购买记录列表查询
    List<PurchaseRecord> listPurchaseRecord(String name, Date startTime, Date endTime, int calculateFlag);

    PurchaseRecord getPurchaseRecordById(Long id);

}