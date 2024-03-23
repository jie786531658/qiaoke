package com.sunshine.qiaoke.Dao;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

// 购买人表
@Data
public class Buyer {

    // 主键
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 联系方式
     */
    private String phone;

    // 购买份额
    private Integer buyCount;

    //结算时使用的临时份额，结转后回写到buyCount
    private Integer tempCount;

    /**
     * 注册时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date registrationTime;

    /**
     * 会员等级
     */
    private Integer vipLevel;

    //结算时使用的临时会员等级，结转后回写到vipLevel
    private Integer tempLevel;

    /**
     * 介绍人ID
     */
    private Long introducerId;

    /**
     * 介绍人
     */
    @TableField(exist = false)
    private String introducerName;



    /**
     * 删除标志
     */
    @TableLogic
    private Integer deleteFlag;


    // 会员等级
    @TableField(exist = false)
    private String vipLevelDesc;

    // 第一级子集
    @TableField(exist = false)
    private List<Buyer> child;

}