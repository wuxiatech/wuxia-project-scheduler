package cn.wuxia.project.scheduler;

import cn.wuxia.project.scheduler.handler.AsyncJob;

public class TestJob extends AsyncJob {


    public void test() {
        logger.info("执行:{}, {}",system,  disableJob);
    }
}
