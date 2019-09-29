/*
* Created on :2016年5月26日
* Author     :Administrator
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.tech All right reserved.
*/
package cn.wuxia.project.scheduler.core.dao;

import cn.wuxia.project.basic.core.common.BaseCommonMongoDao;
import cn.wuxia.project.scheduler.core.entity.ScheduleJob;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ScheduleJobMongoDao extends BaseCommonMongoDao<ScheduleJob, String> {

    public List<ScheduleJob> findInit(String systemType) {
        Criteria criteria = new Criteria();
        criteria.orOperator(Criteria.where("status").is(ScheduleJob.JobState.NONE), Criteria.where("status").is(ScheduleJob.JobState.NORMAL));
        Query query = Query.query(Criteria.where("runSystem").is(systemType).andOperator(criteria));
        query.with(new Sort("order_"));
        return find(query);
    }

    public List<ScheduleJob> find(String systemType, ScheduleJob.JobState state) {
        Query query = Query.query(Criteria.where("runSystem").is(systemType).and("status").is(state));
        return find(query);
    }
}
