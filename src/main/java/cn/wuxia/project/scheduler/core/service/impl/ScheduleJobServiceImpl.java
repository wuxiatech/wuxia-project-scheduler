package cn.wuxia.project.scheduler.core.service.impl;

import cn.wuxia.common.exception.AppDaoException;
import cn.wuxia.common.exception.AppServiceException;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.project.common.dao.CommonMongoDao;
import cn.wuxia.project.common.service.impl.CommonMongoServiceImpl;
import cn.wuxia.project.scheduler.core.dao.ScheduleJobMongoDao;
import cn.wuxia.project.scheduler.core.entity.ScheduleJob;
import cn.wuxia.project.scheduler.core.service.ScheduleJobService;
import cn.wuxia.project.scheduler.util.ScheduleUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author songlin.li
 * @since 2017-07-10
 */
@Service
public class ScheduleJobServiceImpl extends CommonMongoServiceImpl<ScheduleJob, String> implements ScheduleJobService {

    @Autowired
    ScheduleJobMongoDao scheduleJobMongoDao;

    @Autowired
    private Scheduler scheduler;

    /**
     * 当前系统
     */
    @Value("${system.type}")
    protected String system;

    @Override
    public void init() {
        Assert.notNull(system, "application.properties system.type不能为空。");
        List<ScheduleJob> scheduleJobs = scheduleJobMongoDao.findInit(system);
        for (ScheduleJob scheduleJob : scheduleJobs) {

            CronTrigger cronTrigger;
            try {
                cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getName(), scheduleJob.getGroup());
                if (cronTrigger != null) {
                    // 已存在，那么更新相应的定时设置
                    updateTask(scheduleJob);
                } else {
                    createTask(scheduleJob);
                }
            } catch (SchedulerException e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public ScheduleJob save(ScheduleJob job) {
        try {
            if (StringUtil.isBlank(job.getId())) {
                createTask(job);
            } else {
                ScheduleUtils.updateScheduleJob(scheduler, job);
            }
            super.save(job);
        } catch (SchedulerException | AppDaoException e) {
            throw new AppServiceException("启动失败", e);
        }
        return job;
    }

    @Override
    public void delete(String id) {
        delete(findById(id));
    }

    @Override
    public void delete(ScheduleJob scheduleJob) {

        if (StringUtil.equalsIgnoreCase(system, scheduleJob.getRunSystem())) {
            try {
                ScheduleUtils.deleteScheduleJob(scheduler, scheduleJob);
            } catch (SchedulerException e) {
                throw new AppServiceException("{}删除失败", e, scheduleJob.getName());
            }
            super.delete(scheduleJob.getId());
        } else {
            //否则，只改变记录状态, 等待其他系统异步启动
            scheduleJob.setStatus(ScheduleJob.JobState.DELETED);
            try {
                super.save(scheduleJob);
            } catch (AppDaoException e) {
                throw new AppServiceException("{}删除失败", e, scheduleJob.getName());
            }
        }
    }

    @Override
    public void runonceTask(String id) {
        runonceTask(findById(id));

    }

    @Override
    public void startTask(ScheduleJob scheduleJob) {
        if (scheduleJob == null) {
            return;
        }
        /**
         * 如果是当前系统的任务，同步执行
         */
        if (StringUtil.equalsIgnoreCase(system, scheduleJob.getRunSystem())) {
            //当前任务为暂停状态方可启动
            ScheduleJob.JobState jobState = getTaskStatus(scheduleJob);
            if (ScheduleJob.JobState.PAUSED.compareTo(jobState) == 0) {
                try {
                    ScheduleUtils.resumeJob(scheduler, scheduleJob);
                    jobState = getTaskStatus(scheduleJob);
                    scheduleJob.setStatus(jobState);
                    super.save(scheduleJob);
                    logger.info("启动成功：{}, {}", scheduleJob.getAliasName(), scheduleJob.getStatus().getDisplayName());
                } catch (SchedulerException e) {
                    jobState = getTaskStatus(scheduleJob);
                    logger.warn("当前任务：{}，状态：{}, 启动失败信息：{}", scheduleJob.getAliasName(), jobState, e.getMessage());
                    scheduleJob.setStatus(ScheduleJob.JobState.ERROR);
                    try {
                        super.save(scheduleJob);
                    } catch (AppDaoException ex) {
                        throw new AppServiceException("启动失败", ex);
                    }
                } catch (AppDaoException e) {
                    throw new AppServiceException("启动失败", e);
                }
            } else {
                logger.warn("当前任务：{}，状态：{}, 当前任务配置状态：{}", scheduleJob.getAliasName(), getTaskStatus(scheduleJob), scheduleJob.getStatus().getDisplayName());
            }

        } else {
            //否则，只改变记录状态, 等待其他系统异步启动
            scheduleJob.setStatus(ScheduleJob.JobState.NORMAL);
            try {
                super.save(scheduleJob);
            } catch (AppDaoException e) {
                throw new AppServiceException("恢复失败", e);
            }
        }
    }

    @Override
    public void createTask(ScheduleJob scheduleJob) {
        if (scheduleJob == null) {
            return;
        }
        try {
            /**
             * 如果是当前系统的任务，同步执行
             */
            if (StringUtil.equalsIgnoreCase(system, scheduleJob.getRunSystem())) {
                CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getName(), scheduleJob.getGroup());
                if (StringUtil.isBlank(scheduleJob.getId()) || cronTrigger == null) {
                    ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
                    TriggerKey triggerKey = ScheduleUtils.getTriggerKey(scheduleJob.getName(), scheduleJob.getGroup());
                    scheduleJob.setJobTrigger(triggerKey.getName());
                    scheduleJob.setStatus(ScheduleJob.JobState.NORMAL);
                    logger.info("创建成功：{}, {}", scheduleJob.getAliasName(), scheduleJob.getStatus().getDisplayName());
                    super.save(scheduleJob);
                } else {
                    logger.info("当前任务：{}, 状态：{}, 当前任务库配置：{}", scheduleJob.getAliasName(), getTaskStatus(scheduleJob), scheduleJob.getStatus());
                }
            } else {
                //否则，只改变记录状态, 等待其他系统异步启动
                scheduleJob.setStatus(ScheduleJob.JobState.NONE);
                logger.info("创建成功：{}, {}", scheduleJob.getAliasName(), scheduleJob.getStatus().getDisplayName());
                super.save(scheduleJob);
            }
        } catch (SchedulerException | AppDaoException e) {
            throw new AppServiceException("{}创建失败", e, scheduleJob.getAliasName());
        }
    }

    @Override
    public void updateTask(ScheduleJob scheduleJob) {
        try {
            CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getName(), scheduleJob.getGroup());
            if (cronTrigger != null && !StringUtil.equals(scheduleJob.getCronExpression(), cronTrigger.getCronExpression())) {
                logger.info("更新任务:{}, 时间配置：原{}， 现{}", scheduleJob.getAliasName(), cronTrigger.getCronExpression(), scheduleJob.getCronExpression());
                // 已存在，那么更新相应的定时设置
                ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
                logger.info("更新任务：{}， {}", scheduleJob.getAliasName(), scheduleJob.getStatus().getDisplayName());
            }
        } catch (SchedulerException e) {
            throw new AppServiceException("{}创建失败", e, scheduleJob.getAliasName());
        }
    }

    @Override
    public void runonceTask(ScheduleJob scheduleJob) {
        if (scheduleJob == null) {
            return;
        }
        try {
            ScheduleUtils.runOnce(scheduler, scheduleJob);
        } catch (SchedulerException e) {
            throw new AppServiceException("{}运行失败", e, scheduleJob.getAliasName());
        }
    }

    @Override
    public void startTask(String id) {
        startTask(findById(id));
    }

    @Override
    public void paulTask(ScheduleJob scheduleJob) {
        if (scheduleJob == null) {
            return;
        }
        /**
         * 如果是当前系统的任务， 同步执行
         */
        if (StringUtil.equalsIgnoreCase(system, scheduleJob.getRunSystem())) {
            /**
             * 判断当前任务为启动状态方可暂停
             */
            if (ScheduleJob.JobState.NORMAL.compareTo(getTaskStatus(scheduleJob)) == 0) {
                try {
                    ScheduleUtils.pauseJob(scheduler, scheduleJob);
                    scheduleJob.setStatus(getTaskStatus(scheduleJob));
                    super.save(scheduleJob);
                } catch (SchedulerException | AppDaoException e) {
                    throw new AppServiceException("{}暂停失败", e, scheduleJob.getAliasName());
                }
                logger.info("暂停成功：{}, {}", scheduleJob.getAliasName(), scheduleJob.getStatus().getDisplayName());
            } else {
                logger.info("当前任务：{}, 状态：{}, 当前任务库配置：{}", scheduleJob.getAliasName(), getTaskStatus(scheduleJob), scheduleJob.getStatus());
            }
        } else {
            //否则，只改变记录状态, 等待其他系统异步暂停
            scheduleJob.setStatus(ScheduleJob.JobState.PAUSED);
            try {
                super.save(scheduleJob);
            } catch (AppDaoException e) {
                throw new AppServiceException("{}暂停失败", e, scheduleJob.getAliasName());
            }
        }
    }

    @Override
    public void paulTask(String id) {
        paulTask(findById(id));

    }

    @Override
    public void resumeTask(ScheduleJob scheduleJob) {
        if (scheduleJob == null) {
            return;
        }
        /**
         * 如果是当前系统的任务， 同步执行
         */
        if (StringUtil.equalsIgnoreCase(system, scheduleJob.getRunSystem())) {
            /**
             * 判断当前任务为启动状态方可暂停
             */
            if (ScheduleJob.JobState.ERROR.compareTo(getTaskStatus(scheduleJob)) == 0) {
                try {
                    ScheduleUtils.resumeJob(scheduler, scheduleJob);
                    scheduleJob.setStatus(getTaskStatus(scheduleJob));
                    super.save(scheduleJob);
                } catch (SchedulerException | AppDaoException e) {
                    throw new AppServiceException("{}重新开始失败", e, scheduleJob.getAliasName());
                }
                logger.info("重新开始成功：{}, {}", scheduleJob.getAliasName(), scheduleJob.getStatus().getDisplayName());
            } else {
                logger.info("当前任务：{}, 状态：{}, 当前任务库配置：{}", scheduleJob.getAliasName(), getTaskStatus(scheduleJob), scheduleJob.getStatus());
            }
        } else {
            //否则，只改变记录状态, 等待其他系统异步恢复
            scheduleJob.setStatus(ScheduleJob.JobState.RESUME);
            try {
                super.save(scheduleJob);
            } catch (AppDaoException e) {
                throw new AppServiceException("{}重新开始失败", e, scheduleJob.getAliasName());
            }
        }
    }

    @Override
    public void resumeTask(String id) {
        resumeTask(findById(id));
    }

    @Override
    public ScheduleJob.JobState getTaskStatus(String id) {
        return getTaskStatus(findById(id));
    }

    @Override
    public ScheduleJob.JobState getTaskStatus(ScheduleJob scheduleJob) {
        TriggerKey triggerKey = ScheduleUtils.getTriggerKey(scheduleJob.getName(), scheduleJob.getGroup());
        try {
            Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
            return ScheduleJob.JobState.valueOf(triggerState.name());
        } catch (SchedulerException e) {
            throw new AppServiceException(e.getMessage());
        }
    }


    @Override
    protected CommonMongoDao<ScheduleJob, String> getCommonDao() {
        return scheduleJobMongoDao;
    }
}
