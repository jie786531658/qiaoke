package com.sunshine.qiaoke.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunshine.qiaoke.Dao.Buyer;
import com.sunshine.qiaoke.Dao.VipLevelDic;
import com.sunshine.qiaoke.common.Msg;
import com.sunshine.qiaoke.service.BuyerService;
import com.sunshine.qiaoke.service.VipLevelDicService;
import com.sunshine.qiaoke.vo.BuyerListQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.util.List;

@Controller
@RequestMapping("/buyer")
public class BuyerController {

    @Autowired
    private BuyerService buyerService;

    @Autowired
    private VipLevelDicService vipLevelDicService;

    @RequestMapping("/buyerList1")
    @ResponseBody
    public Msg getBuyerList1(@RequestParam(value = "name", required = false) String name) {
        LambdaQueryWrapper<Buyer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null && !name.equals(""), Buyer::getName, name);
        List<Buyer> buyerList = buyerService.list(wrapper);
        return Msg.success().add("buyerList", buyerList);
    }

    @RequestMapping("/buyerList")
    public String getBuyerList(@RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                               Model model) {
        PageHelper.startPage(pageNum, pageSize);
        List<Buyer> buyerList = buyerService.listBuyer(name);
        PageInfo<Buyer> pageInfo = new PageInfo<>(buyerList, 5);
        model.addAttribute("buyerList", buyerList);
        model.addAttribute("buyerPageInfo", pageInfo);
        return "buyer";
    }

    @GetMapping("/getBuyerById/{id}")
    @ResponseBody
    public Msg getBuyerById(@PathVariable("id") String id) {
        Buyer buyer = buyerService.getBuyer(new Long(id));
        return Msg.success().add("buyer", buyer);
    }

    @PostMapping("/addBuyer")
    @ResponseBody
    public Msg addBuyer(Buyer buyer) {
        VipLevelDic vipLevelDic = vipLevelDicService.getLevelAndBonusByJudgeCount(buyer.getBuyCount());
        buyer.setVipLevel(vipLevelDic.getVipLevel());
        buyer.setTempCount(buyer.getBuyCount());
        buyer.setTempLevel(buyer.getVipLevel());
        if(buyer.getIntroducerId() == null){
            buyer.setIntroducerId(10000L);
        }
        Integer result = buyerService.addBuyer(buyer);
        if (result == 1) {
            return Msg.success();
        }
        return Msg.fail();
    }

    @PutMapping("/updateBuyer/{id}")
    @ResponseBody
    public Msg updateBuyer(@PathVariable("id") String id, Buyer buyer) {
        VipLevelDic vipLevelDic = vipLevelDicService.getLevelAndBonusByJudgeCount(buyer.getBuyCount());
        buyer.setVipLevel(vipLevelDic.getVipLevel());
        buyer.setTempCount(buyer.getBuyCount());
        buyer.setTempLevel(buyer.getVipLevel());
        buyer.setId(new Long(id));
        Integer result = buyerService.updateBuyer(buyer);
        if (result == 1) {
            return Msg.success();
        }
        return Msg.fail();
    }

    @RequestMapping("/delBuyer/{id}")
    @ResponseBody
    public Msg delBuyer(@PathVariable("id") String id) {
        Integer result = buyerService.delBuyer(new Long(id));
        if (result == 1) {
            return Msg.success();
        }
        return Msg.fail();
    }

}