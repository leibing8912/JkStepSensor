package cn.jianke.jkstepsensor.module.service;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import java.util.Date;
import cn.jianke.jkstepsensor.common.Constant;
import cn.jianke.jkstepsensor.common.data.DataCache;
import cn.jianke.jkstepsensor.common.data.bean.StepModel;
import cn.jianke.jkstepsensor.common.utils.DateUtils;
import cn.jianke.jkstepsensor.module.core.StepDcretor;

/**
 * @className: StepService
 * @classDescription: 计步服务
 * @author: leibing
 * @createTime: 2016/08/31
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class StepService extends Service implements SensorEventListener {
    // TAG
    private final String TAG = "StepService";
    // 停止广播动作
    public static final String ACTION_STOP_SERVICE = "action_stop_service";
    // step key
    public final static String STEP_KEY = "step_key";
    // 传感器管理
    private SensorManager sensorManager;
    // 计步核心类
    private StepDcretor stepDetector;
    // 自定义Handler
    private MsgHandler msgHandler = new MsgHandler();
    // Messenger 用于跨进程通信
    private Messenger messenger = new Messenger(msgHandler);
    // 计步需要缓存的数据
    private StepModel mStepModel;
    // 计步服务广播
    private BroadcastReceiver stepServiceReceiver;

    /**
     * @className: MsgHandler
     * @classDescription: 用于更新客户端UI
     * @author: leibing
     * @createTime: 2016/08/31
     */
    class MsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MSG_FROM_CLIENT:
                    try {
                        // 缓存数据
                        cacheStepData(StepService.this,StepDcretor.CURRENT_STEP + "");
                        // 回复消息给Client
                        Messenger messenger = msg.replyTo;
                        Message replyMsg = Message.obtain(null, Constant.MSG_FROM_SERVER);
                        Bundle bundle = new Bundle();
                        bundle.putInt(STEP_KEY, StepDcretor.CURRENT_STEP);
                        replyMsg.setData(bundle);
                        messenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化计步服务广播
        initStepServiceReceiver();
        // 启动计步
        startStep();
        Log.v(TAG,"onCreate");
    }

    /**
     * 初始化计步服务广播
     * @author leibing
     * @createTime 2016/09/01
     * @lastModify 2016/09/01
     * @param
     * @return
     */
    private void initStepServiceReceiver() {
        final IntentFilter filter = new IntentFilter();
        // 添加停止当前服务广播动作
        filter.addAction(ACTION_STOP_SERVICE);
        // 实例化广播接收器
        stepServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_STOP_SERVICE.equals(action)){
                    Log.v(TAG,"停止服务");
                    // 停止服务
                    StepService.this.stopSelf();
                }
            }
        };
        // 注册计步服务广播
        registerReceiver(stepServiceReceiver, filter);
    }

    /**
     * 启动计步
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param
     * @return
     */
    private void startStep() {
        // 启动计步器
        startStepDetector();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");
        return START_STICKY;
    }

    /**
     * 缓存计步数据
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param context 上下文
     * @param stepCount 计步数
     * @return
     */
    private void cacheStepData(Context context, String stepCount){
        mStepModel = new StepModel();
        mStepModel.setDate(DateUtils.simpleDateFormat(new Date()));
        mStepModel.setStep(stepCount);
        DataCache.getInstance().addStepCache(context, mStepModel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind");
        return messenger.getBinder();
    }

    /**
     * 启动计步器
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param
     * @return
     */
    private void startStepDetector() {
        if (sensorManager != null && stepDetector != null) {
            sensorManager.unregisterListener(stepDetector);
            sensorManager = null;
            stepDetector = null;
        }
        // 初始化计步(拿缓存更新计步数)
        DataCache.getInstance().getTodayCache(this, new DataCache.DataCacheListener() {
            @Override
            public void readListCache(StepModel stepModel) {
                if (stepModel != null){
                   StepDcretor.CURRENT_STEP = Integer.parseInt(stepModel.getStep());
                }
            }
        });

        sensorManager = (SensorManager) this
                .getSystemService(SENSOR_SERVICE);
        // 添加自定义
        addBasePedoListener();
        // 添加传感器监听
        addCountStepListener();
    }

    /**
     * 停止计步器
     * @author leibing
     * @createTime 2016/09/01
     * @lastModify 2016/09/01
     * @param
     * @return
     */
    public void stopStepDetector(){
        if (sensorManager != null && stepDetector != null) {
            sensorManager.unregisterListener(stepDetector);
            sensorManager = null;
            stepDetector = null;
        }
    }

    /**
     * 添加传感器监听（步行检测传感器、计步传感器）
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param
     * @return
     */
    private void addCountStepListener() {
        // 步行检测传感器，用户每走一步就触发一次事件
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        // 计步传感器
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (detectorSensor != null) {
            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_UI);
        } else if (countSensor != null) {
            sensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.v(TAG, "Count sensor not available!");
        }
    }

    /**
     *添加传感器监听(加速度传感器)
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param
     * @return
     */
    private void addBasePedoListener() {
        stepDetector = new StepDcretor();
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        Sensor sensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // sensorManager.unregisterListener(stepDetector);
        sensorManager.registerListener(stepDetector, sensor,
                SensorManager.SENSOR_DELAY_UI);
        stepDetector
                .setOnSensorChangeListener(new StepDcretor.OnSensorChangeListener() {

                    @Override
                    public void onChange() {
                    }
                });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        // 取消前台进程
        stopForeground(true);
        // 解注册计步服务广播
        unregisterReceiver(stepServiceReceiver);
        // 停止计步器
        stopStepDetector();

        Log.v(TAG,"onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG,"onUnbind");
        return super.onUnbind(intent);
    }
}
