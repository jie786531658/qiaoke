package com.sunshine.qiaoke.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunshine.qiaoke.Dao.Earnings;
import com.sunshine.qiaoke.Dao.PurchaseRecord;
import com.sunshine.qiaoke.common.Msg;
import com.sunshine.qiaoke.service.BuyerService;
import com.sunshine.qiaoke.service.EarningsService;
import com.sunshine.qiaoke.service.PurchaseRecordService;
import com.sunshine.qiaoke.vo.BuyerVo;
import com.sunshine.qiaoke.vo.CalEarnings2Pojo;
import com.sunshine.qiaoke.vo.CalEarningsPojo;
import com.sunshine.qiaoke.vo.EarningsQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.script.ScriptException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("/earnings")
public class EarningsController {

    @Autowired
    private BuyerService buyerService;

    @Autowired
    private PurchaseRecordService recordService;

    @Autowired
    private EarningsService earningsService;

/*    @RequestMapping("/earningsList1")
    public String getEarningsList1(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                   @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                                   Model model) {
        PageHelper.startPage(pageNum, pageSize);
        List<Earnings> earningsList = earningsService.listEarnings(new EarningsQuery());
        PageInfo<Earnings> pageInfo = new PageInfo<>(earningsList);
        model.addAttribute("earningsList", earningsList);
        model.addAttribute("pageInfo", pageInfo);
        return "earnings";
    }*/

    @RequestMapping("/earningsList")
    public String getEarningsList(@RequestParam(value = "name", required = false) String name,
                                  @RequestParam(value = "calFlag", required = true, defaultValue = "1") int calFlag,
                                  @RequestParam(value = "startTime", required = true, defaultValue = "2024-01-01 00:00") String startTime,
                                  @RequestParam(value = "endTime", required = true, defaultValue = "2099-01-01 00:00") String endTime,
                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                                  Model model) throws ParseException {
        EarningsQuery query = new EarningsQuery();
        if (name != null || !StringUtils.isEmpty(name)) {
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
    public Msg getCalStr(@PathVariable("id") String id) throws ScriptException {
        CalEarningsPojo earningsPojo = earningsService.getCalStr(new Long(id));
        return Msg.success().add("earningsPojo", earningsPojo);
    }

    @PostMapping("/calculate")
    @ResponseBody
    public Msg calculate(@RequestBody CalEarnings2Pojo earnings2Pojo) {
//        boolean flag = earningsService.calculate(earningsPojo);
        boolean flag = earningsService.calculate2(earnings2Pojo);
        return flag ? Msg.success() : Msg.fail();
    }

    @RequestMapping("/getSubUser/{id}")
    @ResponseBody
    public Msg getSubUser(@PathVariable("id") String id) {
        LinkedList<Object> list = new LinkedList<>();
        BuyerVo buyerVo = buyerService.getSubBuyer(new Long(id));
        list.add(buyerVo);
        return Msg.success().add("list", list);
    }

    @PostMapping("/jiezhuan")
    @ResponseBody
    @Transactional
    public Msg jiezhuan() {
        LambdaUpdateWrapper<PurchaseRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(PurchaseRecord::getCalculateFlag, 1)
                .eq(PurchaseRecord::getCalculateFlag, 0);
        boolean flag = recordService.update(wrapper);

//        LambdaUpdateWrapper<Buyer> buyerWrapper = new LambdaUpdateWrapper<>();
////        buyerWrapper.set(Buyer::getBuyCount, Buyer::getTempCount);
//        buyerWrapper.set(Buyer::getBuyCount, Buyer::getTempCount)
//                    .set(Buyer::getVipLevel, "tempLevel");
//        buyerService.update(buyerWrapper);
        boolean result = buyerService.jiezhuan();
        return flag && result ? Msg.success() : Msg.fail();
    }

    @RequestMapping("/getCal/{id}")
    @ResponseBody
    public Msg getCal(@PathVariable("id") String id) throws ScriptException {
        CalEarnings2Pojo earnings2Pojo = earningsService.cal(new Long(id));
        return Msg.success().add("earnings2Pojo", earnings2Pojo);
    }

}