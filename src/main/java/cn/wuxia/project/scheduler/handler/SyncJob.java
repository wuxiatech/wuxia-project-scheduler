package cn.wuxia.project.scheduler.handler;

import org.quartz.DisallowConcurrentExecution;

/**
 * 任务工厂类,保证多个任务间不会同时执行.所以在多任务执行时最好加上
 */
@DisallowConcurrentExecution
public class SyncJob extends AsyncJob {

}
