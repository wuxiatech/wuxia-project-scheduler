package cn.wuxia.project.scheduler.handler;

import cn.wuxia.common.spring.SpringContextHolder;
import cn.wuxia.common.util.reflection.ReflectionUtil;
import cn.wuxia.project.scheduler.QuartzHandler;
import cn.wuxia.project.scheduler.util.SchedulerConstants;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务工厂类,并发
 * @author songlin
 */
public class AsyncJob implements Job {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected boolean disableJob = false;
    protected String system;
    public AsyncJob() {
        QuartzHandler quartzHandler = SpringContextHolder.getBean(QuartzHandler.class);
        disableJob = quartzHandler.isDisableSchedule();
        system = quartzHandler.getSystem();
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        JobDataMap mergedJobDataMap = jobExecutionContext.getMergedJobDataMap();
        JobDetailBean detailBean = (JobDetailBean) mergedJobDataMap.get(SchedulerConstants.SCHEDULER_JOB_PARAMETER);
        if (disableJob) {
            logger.warn("已关闭定时任务：{}, {}", getClass().getName(), detailBean != null ? detailBean.getMethod() : "");
            return;
        }
        logger.info("执行:{}.{} ,参数：{}", getClass().getName(), detailBean.getMethod(), detailBean.getParam());
        try {
            ReflectionUtil.invokeMethod(this, detailBean.getMethod(), new Class[]{String.class}, new Object[]{detailBean.getParam()});
        } catch (IllegalArgumentException e) {
            ReflectionUtil.invokeMethod(this, detailBean.getMethod(), new Class[]{}, new Object[]{});
        } catch (Exception e) {
            logger.error("任务执行失败", e);
        }
    }
}
