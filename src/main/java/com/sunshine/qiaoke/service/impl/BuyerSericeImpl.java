package com.sunshine.qiaoke.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunshine.qiaoke.Dao.Buyer;
import com.sunshine.qiaoke.mapper.BuyerMapper;
import com.sunshine.qiaoke.service.BuyerService;
import com.sunshine.qiaoke.vo.BuyerVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class BuyerSericeImpl extends ServiceImpl<BuyerMapper, Buyer> implements BuyerService {
    @Resource
    private BuyerMapper buyerMapper;

    @Override
    public List<Buyer> listBuyer(String name) {
        return buyerMapper.listBuyer(name);
    }

    @Override
    public Integer addBuyer(Buyer buyer) {
        buyer.setRegistrationTime(DateUtil.date());
        return buyerMapper.insert(buyer);
    }
    @Override
    public Integer updateBuyer(Buyer buyer) {
        return buyerMapper.updateById(buyer);
    }

    @Override
    public Integer delBuyer(Long id) {
        return buyerMapper.deleteById(id);
    }

    @Override
    public Buyer getBuyer(Long id) {
        return buyerMapper.selectById(id);
    }

    @Override
    public List<Buyer> getChildren(Long id) {
        return buyerMapper.getChildren(id);
    }

    @Override
    public List<BuyerVo> getTree(Long id) {
        return buyerMapper.getTree(id);
    }

    @Override
    public boolean jiezhuan() {
        Integer result = buyerMapper.jiezhuan();
        return result > 1;
    }

/*    public BuyerVo getSubBuyer(Long id) {
        buyerMapper.getChildren(id);
    }*/


/*    @Override
    public BuyerVo getSubBuyer(Long id) {
        BuyerVo buyerVo = new BuyerVo();
        //查找自身信息
        Buyer buyer = buyerMapper.selectById(id);
        buyerVo.setId(buyer.getId());
        buyerVo.setName(buyer.getName());
        buyerVo.setText(buyer.getName());
        buyerVo.setIntroducerId(buyer.getIntroducerId());

        //查找所有人
        List<Buyer> buyers = buyerMapper.getAllBuyer();
        //下级
        List<BuyerVo> subBuyerVos = new ArrayList<>();
        //找到一层下级
        for (Buyer buyer1 : buyers) {
            if (buyer1.getIntroducerId().equals(buyer.getId())) {
                BuyerVo subBuyerVo = new BuyerVo();
                subBuyerVo.setId(buyer1.getId());
                subBuyerVo.setName(buyer1.getName());
                subBuyerVo.setText(buyer1.getName());
                subBuyerVo.setIntroducerId(buyer1.getIntroducerId());
                subBuyerVos.add(subBuyerVo);
            }
        }

        buyerVo.setNodes(subBuyerVos);
        //找到子级的子级
        findSubBuyer(subBuyerVos, buyers);
        return buyerVo;
    }*/

/*    private void findSubBuyer(List<BuyerVo> subBuyerVos, List<Buyer> buyers) {
        for (BuyerVo subBuyerVo : subBuyerVos) {
            List<BuyerVo> subBuyerVoss = new ArrayList<>();
            for (Buyer buyer : buyers) {
                if (buyer.getIntroducerId().equals(subBuyerVo.getId())) {
                    BuyerVo subBuyerVoo = new BuyerVo();
                    subBuyerVoo.setId(buyer.getId());
                    subBuyerVoo.setName(buyer.getName());
                    subBuyerVoo.setText(buyer.getName());
                    subBuyerVoo.setIntroducerId(buyer.getIntroducerId());
                    subBuyerVoss.add(subBuyerVoo);
                }
                if (!subBuyerVoss.isEmpty()) {
                    subBuyerVo.setNodes(subBuyerVoss);
                }
                findSubBuyer(subBuyerVoss, buyers);
            }
        }
    }*/

}
