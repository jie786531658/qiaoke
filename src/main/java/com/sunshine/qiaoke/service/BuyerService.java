package com.sunshine.qiaoke.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunshine.qiaoke.Dao.Buyer;
import com.sunshine.qiaoke.vo.BuyerVo;

import java.util.Date;
import java.util.List;

public interface BuyerService extends IService<Buyer> {
    // 人员管理列表
    List<Buyer> listBuyer(String name);

    // 查询单个人员
    Buyer getBuyer(Long id);

    // 新增人员
    Integer addBuyer(Buyer buyer);

    // 修改人员
    Integer updateBuyer(Buyer buyer);

    // 删除人员
    Integer delBuyer(Long id);

    // 查询出所有直接下级，不包括下级的下级
    List<Buyer> getChildren(Long id);

    BuyerVo getSubBuyer(Long id);

    boolean jiezhuan();

}