package cn.wuxia.project.scheduler;

import java.util.TimerTask;

/**
 * @author songlin
 */
public class CustomerTimerTask extends TimerTask {
    QuartzHandler quartzHandler;

    public CustomerTimerTask(QuartzHandler quartzHandler) {
        this.quartzHandler = quartzHandler;
    }

    @Override
    public void run() {
        quartzHandler.init();
        quartzHandler.chageLoggerLevel();
    }

}
