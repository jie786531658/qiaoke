package com.sunshine.qiaoke.service;

import com.sunshine.qiaoke.Dao.Earnings;
import com.sunshine.qiaoke.vo.CalEarnings2Pojo;
import com.sunshine.qiaoke.vo.CalEarningsPojo;
import com.sunshine.qiaoke.vo.EarningsQuery;

import javax.script.ScriptException;
import java.util.List;

public interface EarningsService {

    // 结算列表查询
    List<Earnings> listEarnings(EarningsQuery query);

    /**
     * 计算收益、计算方式字符串
     * @param id 待计算收益人员的ID
     * @return 收益、计算方式字符串
     */
    CalEarningsPojo getCalStr(Long id) throws ScriptException;

    /**
     * 1、更新人员表中自身的会员等级和购买份额
     * 2、往收益表中新增相应数据
     * 3、重新计算自己的所有上级的会员等级和购买份额
     * @param calEarningsPojo 待计算收益人员的ID
     * @return 更新是否成功
     */
    boolean calculate(CalEarningsPojo calEarningsPojo);

    /**
     * 1、更新人员表中自身的会员等级、购买份额、临时会员等级、临时购买份额
     * 2、往收益表中新增相应数据
     *
     * @param calEarnings2Pojo 待计算收益人员的ID
     * @return 更新是否成功
     */
    boolean calculate2(CalEarnings2Pojo calEarnings2Pojo);

    CalEarnings2Pojo cal(Long id) throws ScriptException;

}
