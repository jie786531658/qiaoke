package com.sunshine.qiaoke.vo;

import lombok.Data;

/*
计算收益时，使用的临时对象
 */
@Data
public class CalEarningsPojo {

    //人员ID
    private Long id;

    //会员等级
    private int level;

    //奖金
    private int bonus;

    //自身新增份额
    private int myAddCount;

    //所有下级的新增份额
    private int childAddCount;

    //最终新增份额（不包含自身初始份额）
    private int ultiAddCount;

    //收益
    private int earnings;

    //计算公式
    private StringBuilder str = new StringBuilder();

}
