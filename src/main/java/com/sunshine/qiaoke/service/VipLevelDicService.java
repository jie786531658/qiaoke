package com.sunshine.qiaoke.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunshine.qiaoke.Dao.VipLevelDic;

public interface VipLevelDicService extends IService<VipLevelDic> {

    VipLevelDic getVipLevelDicByLevel(Integer level);

    /**
     * 通过判断购买份额，得到对应会员等级、奖金
     * @param count 购买份额
     * @return 对应会员等级、奖金的映射
     */
    VipLevelDic getLevelAndBonusByJudgeCount(Integer count);

    /**
     * 得到对应份额的奖金
     * @param count 份额
     * @return 奖金
     */
    Integer getBonusByCount(Integer count);

}
