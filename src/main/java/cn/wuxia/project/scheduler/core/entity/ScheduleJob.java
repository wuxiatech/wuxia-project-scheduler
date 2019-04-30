package cn.wuxia.project.scheduler.core.entity;

import cn.wuxia.project.common.model.ModifyInfoMongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author songlin.li
 * @since 2017-07-10
 */
@Getter
@Setter
@Document(collection = "schedule_job")
public class ScheduleJob extends ModifyInfoMongoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务别名
     */
    private String aliasName;

    /**
     * 任务分组
     */
    private String group;

    /**
     * 触发器
     */
    private String jobTrigger;

    /**
     * 任务状态
     */
    private JobState status;

    /**
     * 任务运行时间表达式
     */
    private String cronExpression;

    /**
     * 是否异步
     */
    private Boolean isSync;

    /**
     * 任务描述
     */
    private String description;

    private String param;

    /**
     * 要执行的任务类
     */
    private String jobClassName;

    /**
     * 方法名
     */
    private String methodName;

    private Integer order_;

    /**
     * 运行环境
     */
    private String runSystem;

    public static enum JobState {
        NONE("新建"), NORMAL("正常"), PAUSED("已暂停"), COMPLETE("已完成"), ERROR("错误"), BLOCKED("阻塞"),DELETED("已删除");

        private String displayName;

        private JobState(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


}
