package cn.wuxia.project.scheduler.core.service;

import cn.wuxia.project.scheduler.core.entity.ScheduleJob;
import cn.wuxia.project.common.service.CommonService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author songlin.li
 * @since 2017-07-10
 */
public interface ScheduleJobService extends CommonService<ScheduleJob, String> {

    /**
     * 初始化调用,系统启动时调用
     */
    void init();

    /**
     * 创建
     * @param job
     */
    public void createTask(ScheduleJob job);

    /**
     * 更新
     * @param job
     */
    public void updateTask(ScheduleJob job);

    /**
     * 运行一次
     * @param job
     */
    void runonceTask(ScheduleJob job);

    /**
     * 运行一次
     * @param id
     */
    void runonceTask(String id);

    /**
     * 启动
     * @param job
     */
    void startTask(ScheduleJob job);

    /**
     * 启动
     * @param id
     */
    void startTask(String id);

    /**
     * 暂停
     * @param job
     */
    void paulTask(ScheduleJob job);

    /**
     * 暂停
     * @param id
     */
    void paulTask(String id);

    /**
     * 获取实时的状态
     * @param id
     * @return
     */
    ScheduleJob.JobState getTaskStatus(String id);

    /**
     *     获取实时的状态
     * @param scheduleJob
     * @return
     */
    public ScheduleJob.JobState getTaskStatus(ScheduleJob scheduleJob);
}
