package com.sunshine.qiaoke.Dao;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class VipLevelDic {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 会员等级代码
     */
    private Integer vipLevel;

    /**
     * 会员等级说明
     */
    private String vipLevelDesc;

    /**
     * 最小份额
     */
    private Integer minCount;

    /**
     * 最大份额
     */
    private Integer maxCount;

    /**
     * 奖金
     */
    private Integer bonus;
}