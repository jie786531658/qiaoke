package com.sunshine.qiaoke.Dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 购买记录表
 */
@Data
public class PurchaseRecord {
    /**
     * 主键，购买记录ID
     */
    @TableId
    private Long id;

    /**
     * 购买人ID
     */
    private Long buyerId;

    /**
     * 购买数量
     */
    private Integer amount;

    /**
     * 购买时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT + 8")
    private Date buyTime;

    /**
     * 结算标志
     */
    private Integer calculateFlag;

    /**
     * 结算标志描述
     */
    @TableField(exist = false)
    private String calFlagStr;

    /**
     * 删除标志
     */
    @TableLogic
    private Integer deleteFlag;

    // 购买人姓名
    @TableField(exist = false)
    private String name;

    // 会员等级
    @TableField(exist = false)
    private String vipLevelDesc;

    // 介绍人姓名
    @TableField(exist = false)
    private String introducerName;

}