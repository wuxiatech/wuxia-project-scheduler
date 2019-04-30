package cn.wuxia.project.scheduler.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 *
 */
@Setter
@Getter
@AllArgsConstructor
public class JobDetailBean implements Serializable {

    private static final long serialVersionUID = -3212518723715540189L;

    String method;

    String param;


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
