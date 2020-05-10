package cn.wuxia.project.scheduler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import cn.wuxia.common.spring.SpringContextHolder;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.project.basic.core.conf.service.SystemDictionaryService;
import cn.wuxia.project.basic.core.conf.support.DicBean;
import cn.wuxia.project.basic.support.DConstants;
import cn.wuxia.project.scheduler.core.entity.ScheduleJob;
import cn.wuxia.project.scheduler.core.service.ScheduleJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

//@Component
//@EnableScheduling
public class QuartzHandler {
    private static final Logger logger = LoggerFactory.getLogger(QuartzHandler.class);

    //    @Autowired
    protected ScheduleJobService scheduleJobService = SpringContextHolder.getBean(ScheduleJobService.class);

    //    @Autowired
    private SystemDictionaryService dictionaryService = SpringContextHolder.getBean(SystemDictionaryService.class);

    protected String system;
    protected boolean disableSchedule = false;

    public QuartzHandler() {
        logger.info("初始化定时任务");
    }

    /**
     * 定时检查ScheduleJob的状态变化, 注意区分
     */
    //    @Scheduled(cron = "0 0/1 * ? * MON-FRI")
    public void init() {
        logger.info("{}======{}", system, new Date());
        if (StringUtil.isBlank(system) || StringUtil.equals("SYS_BASE_2", system)) {
            return;
        }

        List<ScheduleJob> jobs = scheduleJobService.findBy("runSystem", system);
        for (ScheduleJob job : jobs) {

            scheduleJobService.updateTask(job);

            switch (job.getStatus()) {

                case NONE:
                    scheduleJobService.createTask(job);
                    break;
                case NORMAL:
                    scheduleJobService.startTask(job);
                    break;
                case PAUSED:
                    scheduleJobService.paulTask(job);
                    break;
                case COMPLETE:
                    break;
                case RESUME:
                    scheduleJobService.resumeTask(job);
                    break;
                case BLOCKED:
                    break;
                case DELETED:
                    scheduleJobService.delete(job);
                    break;
            }
        }
    }

    /**
     * 曾经何时，自己蠢到debug为什么方法不执行，原来是自己把cn.wuxia.project的日志动态给warn了
     */
    public void chageLoggerLevel() {

        List<DicBean> loggerList = dictionaryService.findByParentCode(DConstants.LOGGER_LEVEL);
        logger.info("{}执行日志级别任务", system);
        if (ListUtil.isNotEmpty(loggerList)) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            for (DicBean dicBean : loggerList) {
                try {
                    logger.info("修改{}日志级别为{}", dicBean.getCode(), dicBean.getValue());
                    loggerContext.getLogger(dicBean.getCode()).setLevel(Level.valueOf(dicBean.getValue()));
                } catch (Exception e) {
                    logger.error("动态修改[" + dicBean.getCode() + "]日志级别[" + dicBean.getValue() + "]出错", e);
                }
            }
        }
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public boolean isDisableSchedule() {
        return disableSchedule;
    }

    public void setDisableSchedule(boolean disableSchedule) {
        this.disableSchedule = disableSchedule;
    }

}
