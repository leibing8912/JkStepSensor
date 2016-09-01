package cn.jianke.jkstepsensor.common.data.bean;

import java.io.Serializable;

/**
 * @className: StepModel
 * @classDescription: 计步需要缓存的数据
 * @author: leibing
 * @createTime: 2016/08/31
 */
public class StepModel implements Serializable{
    // 序列化UID 用于反序列化
    private static final long serialVersionUID = 1803672514800467436L;
    // 日期 格式如 2016/08/31
    private String date;
    // 步数
    private String step;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }
}
