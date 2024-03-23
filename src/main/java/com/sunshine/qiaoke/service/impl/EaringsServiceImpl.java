package com.sunshine.qiaoke.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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
import com.sunshine.qiaoke.vo.BuyerVo;
import com.sunshine.qiaoke.vo.CalEarnings2Pojo;
import com.sunshine.qiaoke.vo.CalEarningsPojo;
import com.sunshine.qiaoke.vo.EarningsQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sunshine.qiaoke.common.Consts.SELF_BONUS;

@Service
public class EaringsServiceImpl implements EarningsService {

    @Autowired
    private BuyerService buyerService;

    @Autowired
    private PurchaseRecordService recordService;

    @Autowired
    private VipLevelDicService vipLevelDicService;

    @Autowired
    private EarningsMapper earningsMapper;

    @Autowired
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
    public CalEarningsPojo getCalStr(Long id) throws ScriptException {
        CalEarningsPojo ultiEarningsPojo = new CalEarningsPojo();
        StringBuilder str = new StringBuilder(" "); //计算公式
        int earnings = 0; //收益
        //需要参与结算收益的购买记录
        List<PurchaseRecord> myRecordsToCal = recordService.getRecordToCal(id);

        Buyer buyer = buyerService.getBuyer(id);
        int initCount = buyer.getBuyCount(); //初始购买份额
        CalEarningsPojo myEarningsPojo = getCalEarningsPojo(id);
        int myBonus = myEarningsPojo.getBonus();//自身最终会员等级所对应的奖金份额
        ultiEarningsPojo.setLevel(myEarningsPojo.getLevel());
        ultiEarningsPojo.setUltiAddCount(myEarningsPojo.getUltiAddCount() + initCount);

        // 1、计算自身收益
        if (myRecordsToCal != null && !myRecordsToCal.isEmpty()) { // 没有下级且有需要参与结算的购买记录
            int addCount = 0; // 新增购买份额
            for (PurchaseRecord record : myRecordsToCal) {
                addCount += record.getAmount();
            }

            int initLevel = buyer.getVipLevel(); //初始会员等级
            int ultiCount = initCount + addCount; //自身总计购买份额
            ultiEarningsPojo.setUltiAddCount(ultiCount);

            // 计算会员等级、相应奖金
            VipLevelDic vipLevelDic = vipLevelDicService.getLevelAndBonusByJudgeCount(ultiCount);

            int ultiLevel = vipLevelDic.getVipLevel(); //会员等级
            ultiEarningsPojo.setLevel(ultiLevel);
            int bonus = vipLevelDic.getBonus(); //会员等级所对应的奖金

//            earnings = addCount * 90;
            str.append(addCount).append("*90");

            if (ultiLevel <= initLevel) { //如果会员等级未提升，直接计算新增购买记录的收益
//                earnings += addCount * bonus;
                str.append("+").append(addCount).append("*").append(bonus);
            } else { //如果会员等级提升，根据会员等级变动来计算级差收益
                CalEarningsPojo earningsPojo = calVipEarnings(initLevel, ultiLevel, initCount, ultiCount);
//                int vipEarnings = earningsPojo.getEarnings();
                StringBuilder vipStr = earningsPojo.getStr();
//                earnings = earnings + vipEarnings;
                if (String.valueOf(vipStr.charAt(0)).equals("+")) {
                    vipStr.deleteCharAt(0);
                }
                str.append("+").append(vipStr);
            }
        }

        // 2、递归计算下级收益
        CalEarningsPojo childrenEarningsPojo = calChildrenEarnings(id, myBonus, new CalEarningsPojo());
        StringBuilder calStr = childrenEarningsPojo.getStr();
        str.append(calStr);
        //如果自己没有新增购买记录，要去掉从下级得到的计算公式中的+号
        if (String.valueOf(str.charAt(1)).equals("+")) {
            str.deleteCharAt(1);
        }

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine js = manager.getEngineByName("js");
        if (StringUtils.isNotBlank(str)) {
            earnings = (int) js.eval(String.valueOf(str));
            str.append("=").append(earnings);
        } else {
            str.append(0);
        }
//        earnings += childrenEarningsPojo.getEarnings();
        ultiEarningsPojo.setEarnings(earnings);
        String s = str.toString().replace("*", "x");
        str.replace(0, str.length(), s);
        ultiEarningsPojo.setStr(str);
        ultiEarningsPojo.setId(id);
        return ultiEarningsPojo;
    }

    @Override
    public boolean calculate(CalEarningsPojo calEarningsPojo) {
        //1、更新人员表中自身的会员等级和购买份额，
        int ultiLevel = calEarningsPojo.getLevel();
        Long id = calEarningsPojo.getId();
        Buyer buyer = buyerService.getById(id);
        buyer.setVipLevel(ultiLevel);
        buyer.setBuyCount(calEarningsPojo.getUltiAddCount());
        Integer updateInt = buyerService.updateBuyer(buyer);

        //2、往收益表中新增相应数据
        Earnings insertEarnings = new Earnings();
        insertEarnings.setBuyerId(id);
        insertEarnings.setMoney(calEarningsPojo.getEarnings());
        insertEarnings.setVipLevel(ultiLevel);
        insertEarnings.setCalculateTime(DateUtil.date());
        int insertInt = earningsMapper.insert(insertEarnings);

        //3、重新计算自己的所有上级的会员等级和购买份额
//        updateIntroducer(id, calEarningsPojo.getAddCount());
        return updateInt == 1 && insertInt == 1;
    }

    @Override
    public boolean calculate2(CalEarnings2Pojo calEarnings2Pojo) {
        //1、更新人员表中自身的会员等级和购买份额，
        int finalCount = calEarnings2Pojo.getFinalCount();
        VipLevelDic vipLevelDic = vipLevelDicService.getLevelAndBonusByJudgeCount(finalCount);
        int ultiLevel = vipLevelDic.getVipLevel();
        Long id = calEarnings2Pojo.getId();
        Buyer buyer = buyerService.getById(id);
//        buyer.setVipLevel(ultiLevel);
        buyer.setTempLevel(ultiLevel);
//        buyer.setBuyCount(finalCount);
        buyer.setTempCount(finalCount);
        Integer updateInt = buyerService.updateBuyer(buyer);

        //2、往收益表中新增相应数据
        Earnings insertEarnings = new Earnings();
        insertEarnings.setBuyerId(id);
        insertEarnings.setMoney(calEarnings2Pojo.getEarnings());
        insertEarnings.setVipLevel(ultiLevel);
        insertEarnings.setCalculateTime(DateUtil.date());
        int insertInt = earningsMapper.insert(insertEarnings);

        //3、重新计算自己的所有上级的会员等级和购买份额
//        updateIntroducer(id, calEarningsPojo.getAddCount());
        return updateInt == 1 && insertInt == 1;
    }

    /**
     * 计算因会员等级差所产生的收益
     *
     * @param initLevel 初始会员等级
     * @param ultiLevel 最终会员等级
     * @param initCount 初始购买份额
     * @param ultiCount 最终购买份额
     * @return 级差收益、计算方式字符串
     */
    private CalEarningsPojo calVipEarnings(int initLevel, int ultiLevel, int initCount, int ultiCount) {
        CalEarningsPojo earningsPojo = new CalEarningsPojo();
//        int vipEarnings = earningsPojo.getEarnings();
        StringBuilder vipStr = new StringBuilder();
        for (int i = initLevel; i <= ultiLevel; i++) {
            VipLevelDic vipLevelDic = vipLevelDicService.getVipLevelDicByLevel(i);
            int minCount = vipLevelDic.getMinCount();
            int maxCount = vipLevelDic.getMaxCount();
            int bonus = vipLevelDic.getBonus();
            // 计算在每一个会员等级区间内，相应的购买数量所产生的收益，并累加
            if (initCount >= minCount && initCount <= maxCount) {
//                vipEarnings += (maxCount - initCount) * bonus;
                vipStr.append("+(").append(maxCount).append("-").append(initCount).append(")*").append(bonus);
            } else if (ultiCount > maxCount && minCount != 0) {
//                vipEarnings += (maxCount - minCount + 1) * bonus;
                vipStr.append("+(").append(maxCount).append("-").append(minCount - 1).append(")*").append(bonus);
            } else if (ultiCount >= minCount && ultiCount <= maxCount) {
//                vipEarnings += (ultiCount - minCount + 1) * bonus;
                vipStr.append("+(").append(ultiCount).append("-").append(minCount - 1).append(")*").append(bonus);
            }
        }
//        earningsPojo.setEarnings(vipEarnings);
        earningsPojo.setStr(vipStr);
        return earningsPojo;
    }

    /**
     * 递归计算所有下级的收益
     *
     * @param id           人员ID
     * @param bonus        人员最终会员等级所对应的奖金额
     * @param earningsPojo 计算收益时，使用的临时对象
     * @return 计算收益时，使用的临时对象
     */
    private CalEarningsPojo calChildrenEarnings(Long id, int bonus, CalEarningsPojo earningsPojo) {
        StringBuilder str = earningsPojo.getStr();
//        int earnings = earningsPojo.getEarnings();
        // 我的所有下级
        List<Buyer> myChildren = buyerService.getChildren(id);
        for (Buyer child : myChildren) {
            Long childId = child.getId();
            CalEarningsPojo childEarningsPojo = getCalEarningsPojo(childId);
            int childAddCount = childEarningsPojo.getChildAddCount();
            int childBonus = childEarningsPojo.getBonus();
            if (childAddCount != 0) {
//                earnings += (bonus - childBonus) * childAddCount;
                str.append("+(").append(bonus).append("-").append(childBonus).append(")*").append(childAddCount);
//                earningsPojo.setEarnings(earnings);
                earningsPojo.setStr(str);
            }
            List<Buyer> childList = child.getChild();
            if (childList != null && childList.size() > 0) {
                calChildrenEarnings(childId, bonus, earningsPojo);
            }
        }
        return earningsPojo;
    }

    /**
     * 计算最终的新增购买份额，包含自己新增和所有下级新增
     * 计算最终的会员等级以及对应奖金
     *
     * @param id 人员ID
     * @return 相关数据
     */
    private CalEarningsPojo getCalEarningsPojo(Long id) {
        CalEarningsPojo earningsPojo = new CalEarningsPojo();
        // 1、计算自身新增的购买份额
        int myAddcount = getMyAddCount(id);
        // 2、计算下级新增的购买份额
        int childAddCount = getChildAddCount(id);
        // TODO：未加上自身初始份额
        int ultiCount = myAddcount + childAddCount;
        VipLevelDic vipLevelDic = vipLevelDicService.getLevelAndBonusByJudgeCount(ultiCount);
        earningsPojo.setBonus(vipLevelDic.getBonus());
        earningsPojo.setLevel(vipLevelDic.getVipLevel());
        earningsPojo.setMyAddCount(myAddcount);
        earningsPojo.setChildAddCount(childAddCount);
        earningsPojo.setUltiAddCount(ultiCount);
        return earningsPojo;
    }

    /**
     * 计算自身新增购买份额
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
            if (childList != null && childList.size() > 0) {
                childAddcount += getChildAddCount(childId);
            }
        }
        return childAddcount;
    }

    @Override
    public CalEarnings2Pojo cal(Long id) throws ScriptException {
        CalEarnings2Pojo calEarnings2Pojo = new CalEarnings2Pojo();
        StringBuffer s = new StringBuffer();

        //查找自身信息
        Buyer buyer = buyerMapper.selectById(id);
        calEarnings2Pojo.setId(id);
        calEarnings2Pojo.setName(buyer.getName());
        calEarnings2Pojo.setOriginCount(buyer.getBuyCount());
        calEarnings2Pojo.setOriginLevel(buyer.getVipLevel());
        //1.查询所有人
        List<Buyer> allBuyer = buyerMapper.getAllBuyer();
        int myAddCount = getMyAddCount(id);
        calEarnings2Pojo.setMyAddCount(myAddCount);
        calEarnings2Pojo.setMiddleCount(buyer.getBuyCount() + myAddCount);

        //计算自身收益
        // 计算会员等级、相应奖金
        VipLevelDic vipLevelDic = vipLevelDicService.getLevelAndBonusByJudgeCount(calEarnings2Pojo.getMiddleCount());
        calEarnings2Pojo.setMiddleLevel(vipLevelDic.getVipLevel());
        int bonus = vipLevelDic.getBonus();
        //自身奖励
        calEarnings2Pojo.setSelfBonus(SELF_BONUS * calEarnings2Pojo.getMyAddCount());
        s.append(SELF_BONUS * calEarnings2Pojo.getMyAddCount());
        //等级奖励
        if (calEarnings2Pojo.getMiddleLevel()<=calEarnings2Pojo.getOriginLevel()) {
            calEarnings2Pojo.setBonus(myAddCount * bonus);
            s.append("+").append(myAddCount * bonus);


        } else { //如果会员等级提升，根据会员等级变动来计算级差收益
            CalEarnings2Pojo earnings2Pojo = calVipEarnings2(calEarnings2Pojo.getOriginLevel(), calEarnings2Pojo.getMiddleLevel(), calEarnings2Pojo.getOriginCount(), calEarnings2Pojo.getMiddleCount());
            calEarnings2Pojo.setBonus(earnings2Pojo.getBonus());
            s.append("+").append(earnings2Pojo.getStr());
        }

        calEarnings2Pojo.setFinalCount(calEarnings2Pojo.getMiddleCount());
        //4.找到所有下级
//        List<PurchaseRecord> recordList = recordService.getPurchaseRecord();
        BuyerVo buyerVo = buyerService.getSubBuyer(id);
        if (buyerVo.getNodes() != null && buyerVo.getNodes().size() > 0) {
            int allAddCount = 0;
            int childAddCount = getChildAddCount(id);
            allAddCount = calEarnings2Pojo.getMiddleCount() + childAddCount;
            calEarnings2Pojo.setFinalCount(allAddCount);
            VipLevelDic dic = vipLevelDicService.getLevelAndBonusByJudgeCount(calEarnings2Pojo.getFinalCount());
            //我的最终等级 等级奖励
//            int myFinalLevel = dic.getVipLevel();
            int myLevelBonus = dic.getBonus();


            CalEarnings2Pojo tempPojo = new CalEarnings2Pojo();
            tempPojo.setSubEarnings(new AtomicInteger(0));
            //计算从每个下级身上得到的收益
            CalEarnings2Pojo childrenEarningsPojo = calChildrenEarnings2(id, myLevelBonus, tempPojo);
            //TODO 获取的是最后一个人的
            childrenEarningsPojo.setEarnings(childrenEarningsPojo.getSubEarnings().intValue());
            int childrenEarnings = childrenEarningsPojo.getEarnings();
//            String sChildrenEarning = childrenEarningsPojo.getStr().substring(1);
//            ScriptEngineManager manager = new ScriptEngineManager();
//            ScriptEngine js = manager.getEngineByName("js");
//            if (StringUtils.isNotBlank(sChildrenEarning)) {
//                childrenEarnings = (int) js.eval(String.valueOf(sChildrenEarning));
////                str.append("=").append(earnings);
//            } else {
////                str.append(0);
//            }
            //相加得到最终收益
            int allEarnings = calEarnings2Pojo.getSelfBonus() + calEarnings2Pojo.getBonus() + childrenEarnings;
            s.append(childrenEarningsPojo.getStr());
            calEarnings2Pojo.setEarnings(allEarnings);

        } else { //无下级
            calEarnings2Pojo.setEarnings(calEarnings2Pojo.getSelfBonus() + calEarnings2Pojo.getBonus());
        }
        s.append("=").append(calEarnings2Pojo.getEarnings());
        calEarnings2Pojo.setStr(s);
        return calEarnings2Pojo;
    }

    private CalEarnings2Pojo calChildrenEarnings2(Long id, int myLevelBonus, CalEarnings2Pojo calEarnings2Pojo) {
        StringBuffer str = calEarnings2Pojo.getStr();
//        int earnings = calEarnings2Pojo.getEarnings();
        AtomicInteger earnings = calEarnings2Pojo.getSubEarnings();
        // 我的所有下级 先查直接下级
        List<Buyer> myChildren = buyerService.getChildren(id);
        for (Buyer child : myChildren){
            Long childId = child.getId();
            CalEarningsPojo childEarningsPojo = getCalEarningsPojo(childId);
            //我下级新增的数量
            int myAddCount = childEarningsPojo.getMyAddCount();
            //直接下级的下级新增的数量
            int childAddCount = childEarningsPojo.getChildAddCount();
            //直接下级和下级的下级新增的
            int childUltiCount = childEarningsPojo.getUltiAddCount();
            VipLevelDic vipLevelDic = vipLevelDicService.getLevelAndBonusByJudgeCount(child.getBuyCount() + childUltiCount);
            Integer childBonus = vipLevelDic.getBonus();

//            earnings += (myLevelBonus - childBonus) * myAddCount;
            earnings.getAndAdd ((myLevelBonus - childBonus) * myAddCount);
            str.append("+(").append(myLevelBonus).append("-").append(childBonus).append(")*").append(myAddCount);

            calEarnings2Pojo.setStr(str);
//            calEarnings2Pojo.setEarnings(earnings);
            calEarnings2Pojo.setSubEarnings(earnings);
            List<Buyer> childList = child.getChild();
            if(childList != null && childList.size()>0){
                calChildrenEarnings2(childId, myLevelBonus, calEarnings2Pojo);
            }
        }

        return calEarnings2Pojo;
    }

    private int findSubBuyerCount(int allAddCount, List<BuyerVo> subBuyers, List<PurchaseRecord> recordList) {
        //根据购买记录确定新增总量
        for (BuyerVo subBuyerVo : subBuyers) {
            List<BuyerVo> subBuyerVos = subBuyerVo.getNodes();
            int subBuyCount = 0;
            for (PurchaseRecord purchaseRecord : recordList) {
                if (purchaseRecord.getBuyerId().equals(subBuyerVo.getId())) {
                    subBuyCount += purchaseRecord.getAmount();
                    allAddCount += subBuyCount;
                }
                subBuyerVo.setBuyCount(subBuyCount);
                //查找下级的下级
                if (subBuyerVos.size() > 0) {
                    findSubBuyerCount(allAddCount, subBuyerVos, recordList);
                }
            }

        }
        return allAddCount;
    }

    private CalEarnings2Pojo calVipEarnings2(int originLevel, int middleLevel, int originCount, int middleCount) {
        CalEarnings2Pojo earnings2Pojo = new CalEarnings2Pojo();
        int vipBonus = 0;
        StringBuffer vipStr = new StringBuffer();
        for (int i = originLevel; i <= middleLevel; i++){
            VipLevelDic vipLevelDic = vipLevelDicService.getVipLevelDicByLevel(i);
            int minCount = vipLevelDic.getMinCount();
            int maxCount = vipLevelDic.getMaxCount();
            int bonus = vipLevelDic.getBonus();
            // 计算在每一个会员等级区间内，相应的购买数量所产生的收益，并累加
            if (originCount >= minCount && originCount <= maxCount){
                vipBonus += (maxCount - originCount) * bonus;
                vipStr.append("(").append(maxCount).append("-").append(originCount).append(")*").append(bonus);
            } else if (middleCount > maxCount) {
                vipBonus += (maxCount - minCount + 1) * bonus;
                vipStr.append("+(").append(maxCount).append("-").append(minCount - 1).append(")*").append(bonus);
            } else if (middleCount >= minCount && middleCount <= maxCount) {
                vipBonus += (middleCount - minCount + 1) * bonus;
                vipStr.append("+(").append(middleCount).append("-").append(minCount - 1).append(")*").append(bonus);
            }
        }
        earnings2Pojo.setBonus(vipBonus);
        earnings2Pojo.setStr(vipStr);
        return earnings2Pojo;
    }

}
