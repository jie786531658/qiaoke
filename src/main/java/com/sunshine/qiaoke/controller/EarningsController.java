package com.sunshine.qiaoke.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunshine.qiaoke.Dao.Buyer;
import com.sunshine.qiaoke.Dao.Earnings;
import com.sunshine.qiaoke.Dao.PurchaseRecord;
import com.sunshine.qiaoke.common.Msg;
import com.sunshine.qiaoke.service.BuyerService;
import com.sunshine.qiaoke.service.EarningsService;
import com.sunshine.qiaoke.service.PurchaseRecordService;
import com.sunshine.qiaoke.vo.BuyerVo;
import com.sunshine.qiaoke.vo.CalEarningsPojo;
import com.sunshine.qiaoke.vo.EarningsQuery;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("/earnings")
public class EarningsController {

    @Resource
    private BuyerService buyerService;

    @Resource
    private PurchaseRecordService recordService;

    @Resource
    private EarningsService earningsService;

    @RequestMapping("/earningsList")
    public String getEarningsList(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "calFlag", defaultValue = "1") int calFlag, @RequestParam(value = "startTime", defaultValue = "2024-01-01 00:00") String startTime, @RequestParam(value = "endTime", defaultValue = "2099-01-01 00:00") String endTime, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize, Model model) throws ParseException {
        EarningsQuery query = new EarningsQuery();
        if (name != null) {
            query.setName(name);
        }
        if (calFlag == -1) {
            calFlag = 1;
        }
        query.setCalFlag(calFlag);
        String format = "yyyy-MM-dd HH:mm";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date st = dateFormat.parse(startTime);
        query.setStartTime(st);
        Date et = dateFormat.parse(endTime);
        query.setEndTime(et);

        PageHelper.startPage(pageNum, pageSize);
        List<Earnings> earningsList = earningsService.listEarnings(query);
        PageInfo<Earnings> pageInfo = new PageInfo<>(earningsList);
        model.addAttribute("earningsList", earningsList);
        model.addAttribute("pageInfo", pageInfo);
        return "earnings";
    }

    @PostMapping("/getCalStr/{id}")
    @ResponseBody
    public Msg getCalStr(@PathVariable("id") String id) {
        CalEarningsPojo earningsPojo = earningsService.getCalStr(new Long(id));
        return Msg.success().add("earningsPojo", earningsPojo);
    }

    @PostMapping("/calculate")
    @ResponseBody
    public Msg calculate(@RequestBody CalEarningsPojo earningsPojo) {
        boolean flag = earningsService.calculate(earningsPojo);
        return flag ? Msg.success() : Msg.fail();
    }

    @RequestMapping("/getSubUser/{id}")
    @ResponseBody
    public Msg getSubUser(@PathVariable("id") String id) {
        LinkedList<BuyerVo> list = new LinkedList<>();
        Long idL = new Long(id);
        Buyer buyer = buyerService.getById(idL);
        BuyerVo buyerVo = new BuyerVo();
        buyerVo.setId(buyer.getId());
        buyerVo.setText(buyer.getName());
        buyerVo.setNodes(buyerService.getTree(idL));
        list.add(buyerVo);
        return Msg.success().add("list", list);
    }

    @PostMapping("/jiezhuan")
    @ResponseBody
    @Transactional
    public Msg jiezhuan() {
        LambdaUpdateWrapper<PurchaseRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(PurchaseRecord::getCalculateFlag, 1).eq(PurchaseRecord::getCalculateFlag, 0);
        boolean flag = recordService.update(wrapper);
        boolean result = buyerService.jiezhuan();
        return flag && result ? Msg.success() : Msg.fail();
    }

}