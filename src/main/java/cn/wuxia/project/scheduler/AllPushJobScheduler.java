package cn.wuxia.project.scheduler;

import org.apache.commons.lang3.time.DateUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AllPushJobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(AllPushJobScheduler.class);

    public void start() {
        logger.info("start category update notify scheduler");
        schedulerFactoryBean.start();

    }

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    /**
     * 添加任务
     * @param allPushMessage
     */
    public void scheduleNotifyJob(AllPushMessage allPushMessage) {
        if (allPushMessage.getPush_time().compareTo(new Date()) < 0) {
            allPushMessage.setPush_time(DateUtils.addSeconds(new Date(), 10));
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = getJobKey(allPushMessage);
        try {
            if (scheduler.checkExists(jobKey)) {
                logger.info("all push job existed!:" + jobKey.getName());
                return;
            }
        } catch (SchedulerException e) {
            logger.error("get exception:" + e.getMessage(), e);
        }

        logger.info("schedule all push job at:"+allPushMessage.getPush_time() +" with job name pushID" + jobKey.getName());
        JobDataMap jobData = new JobDataMap();
        jobData.put("allPushMessage", allPushMessage);

        JobDetail notifyJob = JobBuilder.newJob(AllPushNotifyJob.class)
                .setJobData(jobData).withIdentity(jobKey).build();
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setName("trigger-" + jobKey.getName());
        trigger.setJobDetail(notifyJob);
        trigger.setStartTime(allPushMessage.getPush_time());
        trigger.setRepeatCount(0);
        trigger.afterPropertiesSet();

        try {
            schedulerFactoryBean.getScheduler().scheduleJob(notifyJob,
                    trigger.getObject());
        } catch (SchedulerException e) {
            logger.error("get exception when executing quartz job" + e);
        }

    }


    /**
     * 删除任务
     * @param allPushMessage
     * @throws Exception
     */
    public void deleteJob(AllPushMessage allPushMessage)throws Exception{

        try{
            //删除定时任务时   先暂停任务，然后再删除
            JobKey jobKey = getJobKey(allPushMessage);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.pauseJob(jobKey);
            scheduler.deleteJob(jobKey);
        }catch(Exception e){
            System.out.println("删除定时任务失败"+e);
            throw new Exception("删除定时任务失败");
        }
    }


    /**
     * 获取jobKey
     * @param allPushMessage
     * @return
     */
    public JobKey getJobKey(AllPushMessage allPushMessage) {

        return JobKey.jobKey(String.valueOf(allPushMessage.getPush_id()));
    }

    /**
     * 更新定时任务
     * @param
     * @param
     * @throws Exception
     */
    public void updateJob(AllPushMessage allPushMessage)throws Exception{
        try {
            TriggerKey triggerKey =getTriggerKey(String.valueOf(allPushMessage.getPush_id()));
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).build();

            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            System.out.println("更新定时任务失败"+e);
            throw new Exception("更新定时任务失败");
        }
    }

    /**
     * 获取触发器key
     *
     * @param
     * @param
     * @return
     */
    public static TriggerKey getTriggerKey(String jobkey) {

        return TriggerKey.triggerKey(jobkey);
    }



    /**
     * 暂停定时任务
     * @param allPushMessage
     * @throws Exception
     */
    public void pauseJob(AllPushMessage allPushMessage) throws Exception {

        JobKey jobKey = JobKey.jobKey(String.valueOf(allPushMessage.getPush_id()));
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            System.out.println("暂停定时任务失败"+e);
            throw new Exception("暂停定时任务失败");
        }
    }

    /**
     * 恢复任务
     * @param
     * @param
     * @param
     * @throws Exception
     */
    public void resumeJob(AllPushMessage allPushMessage) throws Exception {

        JobKey jobKey = JobKey.jobKey(String.valueOf(allPushMessage.getPush_id()));
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            System.out.println("恢复定时任务失败"+e);
            throw new Exception("恢复定时任务失败");
        }
    }

    /**
     * 运行一次任务
     * @param allPushMessage
     * @throws Exception
     */
    public void runOnce(AllPushMessage allPushMessage) throws Exception {
        JobKey jobKey = JobKey.jobKey(String.valueOf(allPushMessage.getPush_id()));
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            System.out.println("运行任务失败"+e);
            throw new Exception("运行一次定时任务失败");
        }
    }

}
