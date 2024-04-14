package com.sunshine.qiaoke.service.impl;

import cn.hutool.core.date.DateUtil;
import com.sunshine.qiaoke.Dao.Buyer;
import com.sunshine.qiaoke.Dao.Earnings;
import com.sunshine.qiaoke.Dao.PurchaseRecord;
import com.sunshine.qiaoke.Dao.VipLevelDic;
import com.sunshine.qiaoke.mapper.BuyerMapper;
import com.sunshine.qiaoke.mapper.EarningsMapper;
import com.sunshine.qiaoke.service.BuyerService;
import com.sunshine.qiaoke.service.EarningsService;
import com.sunshine.qiaoke.service.PurchaseRecordService;
import com.sunshine.qiaoke.service.VipLevelDicService;
import com.sunshine.qiaoke.vo.CalEarningsPojo;
import com.sunshine.qiaoke.vo.EarningsQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.sunshine.qiaoke.common.Consts.SELF_BONUS;

@Service
public class EaringsServiceImpl implements EarningsService {

    @Resource
    private BuyerService buyerService;

    @Resource
    private PurchaseRecordService recordService;

    @Resource
    private VipLevelDicService vipLevelDicService;

    @Resource
    private EarningsMapper earningsMapper;

    @Resource
    private BuyerMapper buyerMapper;

    @Override
    public List<Earnings> listEarnings(EarningsQuery query) {
        List<Earnings> list = new LinkedList<>();
        int calFlag = query.getCalFlag();
        if (calFlag == 1) {
            list = earningsMapper.listCalEarnings(query.getName(), query.getStartTime(), query.getEndTime());
        } else if (calFlag == 0) {
            list = earningsMapper.listNoCalEarnings(query.getName(), query.getStartTime(), query.getEndTime());
        }
        return list;
    }

    @Override
    public CalEarningsPojo getCalStr(Long id) {
        CalEarningsPojo earningsPojo = new CalEarningsPojo();
        StringBuilder str = new StringBuilder(); //计算公式

        // 1.计算自身收益
        Map<String, Object> myMap = calMyEarnings(id);
        str.append(myMap.get("str"));
        int earnings = (int) myMap.get("earnings");

        // 2.计算下级收益
        Map<String, Object> childrenMap = calChildrenEarnings(id);
        earnings += (int) childrenMap.get("earnings");
        str.append(childrenMap.get("str")).append("=").append(earnings);

        earningsPojo.setId(id);
        earningsPojo.setStr(str);
        earningsPojo.setEarnings(earnings);
        earningsPojo.setUltiCount((int) childrenMap.get("myCount"));
        return earningsPojo;
    }

    @Override
    public boolean calculate(CalEarningsPojo calEarningsPojo) {
        //1、更新人员表中自身的会员等级和购买份额
        int ultiCount = calEarningsPojo.getUltiCount();
        VipLevelDic vipLevelDic = vipLevelDicService.getLevelAndBonusByJudgeCount(ultiCount);
        int ultiLevel = vipLevelDic.getVipLevel();
        Long id = calEarningsPojo.getId();
        Buyer buyer = buyerService.getById(id);
        buyer.setTempLevel(ultiLevel);
        buyer.setTempCount(ultiCount);
        Integer updateInt = buyerService.updateBuyer(buyer);

        //2、往收益表中新增相应数据
        Earnings insertEarnings = new Earnings();
        insertEarnings.setBuyerId(id);
        insertEarnings.setMoney(calEarningsPojo.getEarnings());
        insertEarnings.setVipLevel(ultiLevel);
        insertEarnings.setCalculateTime(DateUtil.date());
        int insertInt = earningsMapper.insert(insertEarnings);

        return updateInt == 1 && insertInt == 1;
    }

    /**
     * 计算自身收益
     *
     * @param id id
     * @return 收益、计算公式结果map
     */
    private Map<String, Object> calMyEarnings(Long id) {
        Map<String, Object> map = new HashMap<>();
        StringBuilder str = new StringBuilder();
        int earnings = 0;
        // 需要参与结算收益的购买记录
        List<PurchaseRecord> myRecordsToCal = recordService.getRecordToCal(id);
        // 1、计算自身收益
        if (myRecordsToCal != null && !myRecordsToCal.isEmpty()) { //没有下级且有需要参与结算的购买记录
            Buyer buyer = buyerService.getBuyer(id);
            int initCount = buyer.getBuyCount(); //初始购买份额
            int addCount = 0; //新增购买份额
            for (PurchaseRecord record : myRecordsToCal) {
                addCount += record.getAmount();
            }
            earnings = addCount * SELF_BONUS;
            str.append(addCount).append("*").append(SELF_BONUS);

            int ultiCount = initCount + addCount; //自身总计购买份额
            // 计算会员等级、相应奖金
            VipLevelDic vipLevelDic = vipLevelDicService.getLevelAndBonusByJudgeCount(ultiCount);
            int ultiLevel = vipLevelDic.getVipLevel(); //会员等级
            int initLevel = buyer.getVipLevel(); //初始会员等级
            if (ultiLevel <= initLevel) { //如果会员等级未提升，直接计算新增购买记录的收益
                int bonus = vipLevelDic.getBonus(); //会员等级所对应的奖金
                earnings += addCount * bonus;
                str.append("+").append(addCount).append("*").append(bonus);
            } else { //如果会员等级提升，根据会员等级变动来计算级差收益
                Map<String, Object> vipMap = calVipEarnings(initLevel, ultiLevel, initCount, ultiCount);
                StringBuilder vipStr = (StringBuilder) vipMap.get("str");
                Integer vipEarnings = (Integer) vipMap.get("earnings");
                earnings = earnings + vipEarnings;
                str.append(vipStr);
            }
        } else {
            str = new StringBuilder("0");
        }
        map.put("str", str);
        map.put("earnings", earnings);
        return map;
    }

    /**
     * 计算因会员等级差变化所产生的收益
     *
     * @param initLevel 初始会员等级
     * @param ultiLevel 最终会员等级
     * @param initCount 初始购买份额
     * @param ultiCount 最终购买份额
     * @return 级差收益、计算方式字符串
     */
    private Map<String, Object> calVipEarnings(int initLevel, int ultiLevel, int initCount, int ultiCount) {
        Map<String, Object> map = new HashMap<>();
        StringBuilder str = new StringBuilder();
        int earnings = 0;
        for (int i = initLevel; i <= ultiLevel; i++) {
            VipLevelDic vipLevelDic = vipLevelDicService.getVipLevelDicByLevel(i);
            int minCount = vipLevelDic.getMinCount();
            int maxCount = vipLevelDic.getMaxCount();
            int bonus = vipLevelDic.getBonus();
            // 计算在每一个会员等级区间内，相应的购买数量所产生的收益，并累加
            if (initCount >= minCount && initCount <= maxCount) {
                earnings += (maxCount - initCount) * bonus;
                str.append("+(").append(maxCount).append("-").append(initCount).append(")*").append(bonus);
            } else if (ultiCount > maxCount && minCount != 0) {
                earnings += (maxCount - minCount + 1) * bonus;
                str.append("+(").append(maxCount).append("-").append(minCount - 1).append(")*").append(bonus);
            } else if (ultiCount >= minCount && ultiCount <= maxCount) {
                earnings += (ultiCount - minCount + 1) * bonus;
                str.append("+(").append(ultiCount).append("-").append(minCount - 1).append(")*").append(bonus);
            }
        }
        map.put("str", str);
        map.put("earnings", earnings);
        return map;
    }

    /**
     * 计算从下一级得到的收益（不递归到底，只计算第一层级）
     *
     * @param id 人员ID
     * @return 计算收益时，使用的临时对象
     */
    private Map<String, Object> calChildrenEarnings(Long id) {
        HashMap<String, Object> map = new HashMap<>();
        StringBuilder str = new StringBuilder();
        Buyer myBuyer = buyerMapper.selectById(id);
        int myCount = myBuyer.getBuyCount() + getMyAddCount(id); //我的购买份额 = 我的初始份额 + 我的新增份额
        int earnings = 0;
        // 我的所有直接下级，只包含下一级，并非全部下级，并根据cal_order排序
        List<Buyer> myChildren = buyerService.getChildren(id);
        for (Buyer child : myChildren) {
            Long childId = child.getId();
            int childInitCount = buyerMapper.selectById(childId).getBuyCount(); //子集本身的初始购买份额
            int childSelfAddCount = getMyAddCount(childId); //子集本身的新增购买份额
            int childrenAddCount = getChildAddCount(childId); //子集的所有下级的新增购买份额，不包含子集本身的初始购买份额、新增购买份额

            // 子集的会员等级所对应的奖金额为：子集本身的初始购买份额 + 子集本身的新增购买份额 + 子集的所有下级的新增购买份额 之和所对应的奖金额
            int childBonus = vipLevelDicService.getBonusByCount(childInitCount + childSelfAddCount + childrenAddCount);
            myCount += childSelfAddCount + childrenAddCount;
            int myBonus = vipLevelDicService.getBonusByCount(myCount);
            if (childSelfAddCount != 0 || childrenAddCount != 0) {
                earnings += (myBonus - childBonus) * childSelfAddCount;
                str.append("+(").append(myBonus).append("-").append(childBonus).append(")*").append(childSelfAddCount);
            }
        }
        map.put("str", str);
        map.put("earnings", earnings);
        map.put("myCount", myCount);
        return map;
    }

    /**
     * 计算自身新增购买份额，不包含自身初始份额，不包含下级新增份额
     *
     * @param id 人员ID
     * @return 自身新增购买份额
     */
    private int getMyAddCount(Long id) {
        int myAddCount = 0;
        List<PurchaseRecord> recordsToCal = recordService.getRecordToCal(id);
        for (PurchaseRecord record : recordsToCal) {
            myAddCount += record.getAmount();
        }
        return myAddCount;
    }

    /**
     * 递归计算所有下级的新增购买份额
     *
     * @param id 人员ID
     * @return 所有下级的新增购买份额
     */
    private int getChildAddCount(Long id) {
        int childAddcount = 0;
        List<Buyer> children = buyerService.getChildren(id);
        for (Buyer child : children) {
            Long childId = child.getId();
            int myAddCount = getMyAddCount(childId);
            childAddcount += myAddCount;
            List<Buyer> childList = child.getChild();
            if (childList != null && !childList.isEmpty()) {
                childAddcount += getChildAddCount(childId);
            }
        }
        return childAddcount;
    }

}
