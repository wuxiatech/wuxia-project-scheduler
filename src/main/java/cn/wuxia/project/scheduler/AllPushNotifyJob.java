package cn.wuxia.project.scheduler;

import cn.wuxia.common.spring.SpringContextHolder;
import cn.wuxia.common.util.DateUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class AllPushNotifyJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(AllPushNotifyJob.class);

    private AllPushMessageService allPushMessageService;

    public AllPushNotifyJob() {
        allPushMessageService = SpringContextHolder.getBean(AllPushMessageService.class);
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

        AllPushMessage allPushMessage = (AllPushMessage) dataMap.get("allPushMessage");

        Date expectTriggerTime = allPushMessage.getPush_time();

        Date realTriggerTime = new Date();
        logger.info("execute category notify job with expect trigger time:" + DateUtil.format(expectTriggerTime, "yyyy-MM-dd HH:mm:ss Z"));
        logger.info("real notify time:" + DateUtil.format(realTriggerTime, "yyyy-MM-dd HH:mm:ss Z"));
        allPushMessageService.enforceAllPush(allPushMessage);
    }
}
