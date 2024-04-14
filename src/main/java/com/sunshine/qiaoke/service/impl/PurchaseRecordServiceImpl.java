package com.sunshine.qiaoke.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunshine.qiaoke.Dao.PurchaseRecord;
import com.sunshine.qiaoke.mapper.PurchaseRecordMapper;
import com.sunshine.qiaoke.service.PurchaseRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class PurchaseRecordServiceImpl extends ServiceImpl<PurchaseRecordMapper, PurchaseRecord> implements PurchaseRecordService {

    @Resource
    private PurchaseRecordMapper purchaseRecordMapper;

    @Override
    public List<PurchaseRecord> listPurchaseRecord(String name, Date startTime, Date endTime, int calFlag) {
        return purchaseRecordMapper.listPurchaseRecord(name, startTime, endTime, calFlag);
    }

    @Override
    public List<PurchaseRecord> getRecordToCal(Long buyerId) {
        LambdaQueryWrapper<PurchaseRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseRecord::getBuyerId, buyerId).eq(PurchaseRecord::getCalculateFlag, 0);
        return purchaseRecordMapper.selectList(wrapper);
    }

    @Override
    public PurchaseRecord getPurchaseRecord(Long id) {
        return purchaseRecordMapper.getPurchaseRecordById(id);
    }

    @Override
    public Integer addPurchaseRecord(PurchaseRecord record) {
        record.setBuyTime(DateUtil.date());
        return purchaseRecordMapper.insert(record);
    }

    @Override
    public Integer updatePurchaseRecord(PurchaseRecord record) {
        return purchaseRecordMapper.updateById(record);
    }

    @Override
    public Integer delPurchaseRecord(Long id) {
        return purchaseRecordMapper.deleteById(id);
    }

}
