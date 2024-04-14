package com.sunshine.qiaoke.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunshine.qiaoke.Dao.PurchaseRecord;
import com.sunshine.qiaoke.common.Msg;
import com.sunshine.qiaoke.service.PurchaseRecordService;
import com.sunshine.qiaoke.vo.PurchaseRecordQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/purchaseRecord")
public class PurchaseRecordController {

    @Resource
    private PurchaseRecordService purchaseRecordService;

    @RequestMapping("/recordList")
    public String recordList(@RequestParam(value = "name", required = false) String name,
                             @RequestParam(value = "calFlag", defaultValue = "-1") int calFlag,
                             @RequestParam(value = "startTime", defaultValue = "2024-01-01 00:00") String startTime,
                             @RequestParam(value = "endTime", defaultValue = "2099-01-01 00:00") String endTime,
                             @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                             Model model) throws ParseException {
        PurchaseRecordQuery query = new PurchaseRecordQuery();
        if (name != null) {
            query.setName(name);
        }
        query.setCalFlag(calFlag);
        String format = "yyyy-MM-dd HH:mm";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date st = dateFormat.parse(startTime);
        query.setStartTime(st);
        Date et = dateFormat.parse(endTime);
        query.setEndTime(et);

        PageHelper.startPage(pageNum, pageSize);
        List<PurchaseRecord> recordList = purchaseRecordService.listPurchaseRecord(query.getName(), query.getStartTime(), query.getEndTime(), query.getCalFlag());
        PageInfo<PurchaseRecord> pageInfo = new PageInfo<>(recordList);

        model.addAttribute("recordList", recordList);
        model.addAttribute("pageInfo", pageInfo);
        return "purchaseRecord";
    }

    @GetMapping("/getRecordById/{id}")
    @ResponseBody
    public Msg getRecordById(@PathVariable("id") String id) {
        PurchaseRecord record = purchaseRecordService.getPurchaseRecord(new Long(id));
        return Msg.success().add("record", record);
    }

    @PostMapping("/addRecord")
    @ResponseBody
    public Msg addRecord(PurchaseRecord record) {
        Integer result = purchaseRecordService.addPurchaseRecord(record);
        if (result == 1) {
            return Msg.success();
        }
        return Msg.fail();
    }

    @PutMapping("/updateRecord/{id}")
    @ResponseBody
    public Msg updateRecord(@PathVariable("id") String id, PurchaseRecord record) {
        record.setId(new Long(id));
        Integer result = purchaseRecordService.updatePurchaseRecord(record);
        if (result == 1) {
            return Msg.success();
        }
        return Msg.fail();
    }

    @RequestMapping("/delRecord/{id}")
    @ResponseBody
    public Msg delRecord(@PathVariable("id") String id) {
        Integer result = purchaseRecordService.delPurchaseRecord(new Long(id));
        if (result == 1) {
            return Msg.success();
        }
        return Msg.fail();
    }

}
