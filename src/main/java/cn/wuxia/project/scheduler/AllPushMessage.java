package cn.wuxia.project.scheduler;

import java.util.Date;

public class AllPushMessage {
    String push_id;
    Date push_time;


    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }

    public Date getPush_time() {
        return push_time;
    }

    public void setPush_time(Date push_time) {
        this.push_time = push_time;
    }
}
