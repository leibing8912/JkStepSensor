package cn.jianke.jkstepsensor.module.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.jianke.jkstepsensor.R;
import cn.jianke.jkstepsensor.common.Constant;
import cn.jianke.jkstepsensor.module.service.StepService;

/**
 * @className: MainActivity
 * @classDescription: 首页
 * @author: leibing
 * @createTime: 2016/09/01
 */
public class MainActivity extends AppCompatActivity implements Handler.Callback,View.OnClickListener{
    // 请求码
    private final static int REQUEST_CODE = 11;
    // 是否服务停止
    private boolean isStop;
    // 显示步数
    private TextView stepCountTv;
    // 用于与计步服务通信
    private Messenger messenger;
    private Messenger replyMessenger = new Messenger(new Handler(this));
    private Handler delayHandler = new Handler(this);
    // 循环取当前时刻步数的间隔时间
    private long TIME_INTERVAL = 1000;
    // 确定是否退出app
    private int i = 0;
    // 服务连接
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                messenger = new Messenger(service);
                Message msg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                msg.replyTo = replyMessenger;
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // findView
        stepCountTv = (TextView) findViewById(R.id.tv_step_count);
        // onClick
        findViewById(R.id.btn_turnto_switch).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStop) {
            // 启动服务
            startService();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case Constant.MSG_FROM_SERVER:
                int stepCount = message.getData().getInt(StepService.STEP_KEY);
                // 更新界面上的步数
                stepCountTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                stepCountTv.setText(stepCount + "");
                // 循环向服务请求数据
                delayHandler.sendEmptyMessageDelayed(Constant.REQUEST_SERVER, TIME_INTERVAL);
                break;
            case Constant.REQUEST_SERVER:
                try {
                    Message serverMsg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                    serverMsg.replyTo = replyMessenger;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constant.CONTENT_KEY, "今日行走");
                    bundle.putSerializable(Constant.TICKER_KEY, "健客计步");
                    bundle.putSerializable(Constant.CONTENTTITLE_KEY, "健客计步");
                    bundle.putSerializable(Constant.PENDINGCLASS_KEY, MainActivity.class);
                    bundle.putSerializable(Constant.ISONGOING_KEY, true);
                    bundle.putSerializable(Constant.ICON_KEY,R.mipmap.icon);
                    bundle.putSerializable(Constant.NOTIFYID_KEY, R.string.app_name);
                    serverMsg.setData(bundle);
                    messenger.send(serverMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_turnto_switch:
                Intent intent = new Intent();
                intent.putExtra(SwitchActivity.IS_SERVICE_STOP_KEY, isStop);
                intent.setClass(MainActivity.this, SwitchActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        // 解绑服务
        unbindService(conn);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 启动服务
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param
     * @return
     */
    private void startService() {
        final Intent intent = new Intent(this, StepService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE){
            // 判断是否
            isStop = data.getBooleanExtra(SwitchActivity.IS_SERVICE_STOP_KEY, false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0){
            if(i==0){
                i++;
                String quit = getApplication().getString(R.string.comm_quit);
                Toast.makeText(MainActivity.this, quit, Toast.LENGTH_SHORT).show();
            }else if(i==1){
                finish();
            }
        }
        return true;
    }
}
