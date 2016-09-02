package cn.jianke.jkstepsensor.common.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import cn.jianke.customcache.utils.StringUtil;
import cn.jianke.jkstepsensor.R;

/**
 * @className: NotificationUtils
 * @classDescription: 通知栏管理工具类
 * @author: leibing
 * @createTime: 2016/09/02
 */
public class NotificationUtils {
    // sington
    private static NotificationUtils intance;
    // 通知栏管理
    private NotificationManager nm;
    // 通知栏builder
    private NotificationCompat.Builder builder;

    /**
     *
     * @author leibing
     * @createTime 2016/09/02
     * @lastModify 2016/09/02
     * @param context 上下文
     * @return
     */
    private NotificationUtils(Context context){
        builder = new NotificationCompat.Builder(context);
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * sington
     * @author leibing
     * @createTime 2016/09/02
     * @lastModify 2016/09/02
     * @param context 上下文
     * @return
     */
    public static NotificationUtils getInstance(Context context){
        if (intance == null){
            intance = new NotificationUtils(context);
        }
        return intance;
    }

    /**
     * 更新通知栏
     * @author leibing
     * @createTime 2016/09/02
     * @lastModify 2016/09/02
     * @param content 内容
     * @param ticker
     * @param contentTitle 标题
     * @param context 上下文
     * @param pendingClass 点击通知栏可以跳转到的Activity
     * @param isOngoing 是否不可消除 true为不可消除，false为可消除
     * @param notifyId
     * @param icon 头像
     * @param priority 优先级
     * @return
     */
    public void updateNotification(
                                    String content, String ticker ,String contentTitle,
                                   Context context, Class pendingClass,
                                   boolean isOngoing, int notifyId,
                                   int icon,int priority){
        if (builder == null || nm == null)
            return;
        // 设置优化级
        builder.setPriority(priority);
        // 设置点击通知栏可以跳转到的Activity
        if (content != null && pendingClass != null) {
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, pendingClass), 0);
            builder.setContentIntent(contentIntent);
        }
        // 设置ticker
        if (StringUtil.isNotEmpty(ticker))
            builder.setTicker(ticker);
        // 设置icon
        builder.setSmallIcon(icon);
        // 设置标题
        if (StringUtil.isNotEmpty(contentTitle))
            builder.setContentTitle(contentTitle);
        // 设置不可清除
        builder.setOngoing(isOngoing);
        // 设置内容
        if (StringUtil.isNotEmpty(content))
            builder.setContentText(content);
        // 创建notification
        Notification notification = builder.build();
        // 设置notifyId
        nm.notify(notifyId, notification);
    }

    /**
     * 更新通知栏
     * @author leibing
     * @createTime 2016/09/02
     * @lastModify 2016/09/02
     * @param content 内容
     * @param ticker
     * @param contentTitle 标题
     * @param context 上下文
     * @param pendingClass 点击通知栏可以跳转到的Activity
     * @return
     */
    public void updateNotification(
            String content, String ticker ,String contentTitle,
            Context context, Class pendingClass){
        updateNotification(content, ticker, contentTitle,
                context, pendingClass,true,
                R.string.app_name,R.mipmap.ic_launcher,Notification.PRIORITY_MIN);
    }

    /**
     * 更新通知栏
     * @author leibing
     * @createTime 2016/09/02
     * @lastModify 2016/09/02
     * @param content 内容
     * @param ticker
     * @param contentTitle 标题
     * @return
     */
    public void updateNotification(
            String content, String ticker ,String contentTitle){
            updateNotification(content,ticker,contentTitle,
                    null,null,true,
                    R.string.app_name, R.mipmap.ic_launcher,Notification.PRIORITY_MIN);
    }

    /**
     * 更新通知栏
     * @author leibing
     * @createTime 2016/09/02
     * @lastModify 2016/09/02
     * @param content 内容
     * @param ticker
     * @param contentTitle 标题
     * @param icon 头像
     * @return
     */
    public void updateNotification(
            String content, String ticker ,String contentTitle,int icon){
        updateNotification(content,ticker,contentTitle,
                null,null,true,
                R.string.app_name,icon,Notification.PRIORITY_MIN);
    }

    /**
     * 更新通知栏
     * @author leibing
     * @createTime 2016/09/02
     * @lastModify 2016/09/02
     * @param content 内容
     * @return
     */
    public void updateNotification(
            String content){
        updateNotification(content,null,null,
                null,null,true,
                R.string.app_name,R.mipmap.icon,Notification.PRIORITY_MIN);
    }

    /**
     * 清除当前通知栏管理所管理的所有通知
     * @author leibing
     * @createTime 2016/09/02
     * @lastModify 2016/09/02
     * @param
     * @return
     */
    public void clearAllNotification(){
        if (nm != null){
            nm.cancelAll();
        }
    }

    /**
     * 根据通知栏消息id来清除数据
     * @author leibing
     * @createTime 2016/09/02
     * @lastModify 2016/09/02
     * @param id 通知栏消息id
     * @return
     */
    public void clearNotificationById(int id){
        if (nm != null){
            nm.cancel(id);
        }
    }
}
