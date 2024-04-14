package com.sunshine.qiaoke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunshine.qiaoke.Dao.Buyer;
import com.sunshine.qiaoke.vo.BuyerVo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyerMapper extends BaseMapper<Buyer> {

    List<Buyer> listBuyer (String name);

    List<Buyer> getChildren(Long id);

    List<BuyerVo> getTree(Long id);

    List<Buyer> getAllBuyer();

    Integer jiezhuan();

}
