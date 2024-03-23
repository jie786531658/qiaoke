package com.sunshine.qiaoke.Dao;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class Earnings {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 购买人ID
     */
    private Long buyerId;

    /**
     * 结算金额
     */
    private Integer money;

    /**
     * 结算时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT + 8")
    private Date calculateTime;

    /**
     * 结算时会员等级
     */
    private Integer vipLevel;

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