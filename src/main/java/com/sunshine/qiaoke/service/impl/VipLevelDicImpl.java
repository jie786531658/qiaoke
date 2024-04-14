package com.sunshine.qiaoke.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunshine.qiaoke.Dao.VipLevelDic;
import com.sunshine.qiaoke.mapper.VipLevelDicMapper;
import com.sunshine.qiaoke.service.VipLevelDicService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VipLevelDicImpl extends ServiceImpl<VipLevelDicMapper, VipLevelDic> implements VipLevelDicService {

    @Resource
    private VipLevelDicMapper vipLevelDicMapper;

    @Override
    public VipLevelDic getVipLevelDicByLevel(Integer level) {
        LambdaQueryWrapper<VipLevelDic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VipLevelDic::getVipLevel, level);
        return vipLevelDicMapper.selectOne(wrapper);
    }

    @Override
    public VipLevelDic getLevelAndBonusByJudgeCount(Integer count) {
        VipLevelDic vipDic = new VipLevelDic();
        List<VipLevelDic> vipLevelDicList = vipLevelDicMapper.selectList(new QueryWrapper<>());
        for (VipLevelDic vipLevelDic : vipLevelDicList) {
            Integer minCount = vipLevelDic.getMinCount();
            Integer maxCount = vipLevelDic.getMaxCount();
            if (count >= minCount && count <= maxCount){
                vipDic.setVipLevel(vipLevelDic.getVipLevel());
                vipDic.setBonus(vipLevelDic.getBonus());
                break;
            }
        }
        return vipDic;
    }

    @Override
    public Integer getBonusByCount(Integer count) {
        int bonus = 0;
        List<VipLevelDic> vipLevelDicList = vipLevelDicMapper.selectList(new QueryWrapper<>());
        for (VipLevelDic vipLevelDic : vipLevelDicList) {
            Integer minCount = vipLevelDic.getMinCount();
            Integer maxCount = vipLevelDic.getMaxCount();
            if (count >= minCount && count <= maxCount){
                bonus = vipLevelDic.getBonus();
                break;
            }
        }
        return bonus;
    }

}
