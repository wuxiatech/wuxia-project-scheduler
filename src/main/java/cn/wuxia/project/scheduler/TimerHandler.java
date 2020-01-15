package cn.wuxia.project.scheduler;

import cn.wuxia.project.scheduler.handler.AsyncJob;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//@Component
public class TimerHandler implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(AsyncJob.class);

    @Resource
    private QuartzHandler quartzHandler;

    public void run(){
        logger.info("初始化定时任务");
        ScheduledExecutorService mService = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2,
                new BasicThreadFactory.Builder().namingPattern("scheduler-pool-%d").daemon(true).build());
        mService.scheduleAtFixedRate(() -> {
            quartzHandler.init();
            quartzHandler.chageLoggerLevel();
        }, 10, 60, TimeUnit.SECONDS);
    }

    public QuartzHandler getQuartzHandler() {
        return quartzHandler;
    }

    public void setQuartzHandler(QuartzHandler quartzHandler) {
        this.quartzHandler = quartzHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        run();
    }
}
