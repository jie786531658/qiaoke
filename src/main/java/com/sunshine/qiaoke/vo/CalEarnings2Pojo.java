package com.sunshine.qiaoke.vo;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/*
计算收益时，使用的临时对象
 */
@Data
public class  CalEarnings2Pojo {

    private Long id;

    private String name;

    //会员等级
    private int originLevel;

    //初始份额
    private int originCount;

    //自身新增后等级
    private int middleLevel;

    private int middleCount;

    private int selfBonus;

    //奖金
    private int bonus;

    //自身新增份额
    private int myAddCount;

    //新增份额
    private int addCount;

    //最终份额
    private int finalCount;

    //收益
    private int earnings;

    private AtomicInteger subEarnings;

    //计算公式
    private StringBuffer str = new StringBuffer();

}
