package cn.jianke.jkstepsensor.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @className: DateUtils
 * @classDescription: 时间工具类
 * @author: leibing
 * @createTime: 2016/08/31
 */
public class DateUtils {

    /**
     * 将日期格式转化yyyy/MM/dd样式的字符串
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param date
     * @return
     */
    public static String simpleDateFormat(Date date){
        //格式化当前系统日期
        SimpleDateFormat dateFm = new SimpleDateFormat("yyyy/MM/dd");
        String dateTime = dateFm.format(date).trim().toString();
        return dateTime;
    }

    /**
     * 获取当前时间前一天日期
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param date 日期
     * @return
     */
    public static Date getNextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        return date;
    }
}
