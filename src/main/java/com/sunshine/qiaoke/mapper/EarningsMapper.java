package com.sunshine.qiaoke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunshine.qiaoke.Dao.Earnings;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EarningsMapper extends BaseMapper<Earnings> {

    List<Earnings> listCalEarnings(String name , Date startTime, Date endTime);

    List<Earnings> listNoCalEarnings(String name , Date startTime, Date endTime);

}
